package org.homeschoolpebt.app.cli;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import formflow.library.data.UserFile;
import formflow.library.data.UserFileRepositoryService;
import formflow.library.pdf.PdfService;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.upload.CloudFile;
import org.homeschoolpebt.app.upload.ReadOnlyCloudFileRepository;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TransmitterCommands {

  private final TransmissionRepositoryService transmissionRepositoryService;
  private final TransmissionRepository transmissionRepository;
  private final SubmissionRepository submissionRepository;
  private final PdfService pdfService;

  private final UserFileRepositoryService uploadedFileRepositoryService;
  private final ReadOnlyCloudFileRepository fileRepository;

  public TransmitterCommands(TransmissionRepositoryService transmissionRepositoryService, TransmissionRepository transmissionRepository, SubmissionRepository submissionRepository,
    PdfService pdfService, UserFileRepositoryService uploadedFileRepositoryService, ReadOnlyCloudFileRepository fileRepository) {
    this.transmissionRepositoryService = transmissionRepositoryService;
    this.transmissionRepository = transmissionRepository;
    this.submissionRepository = submissionRepository;
    this.pdfService = pdfService;
    this.uploadedFileRepositoryService = uploadedFileRepositoryService;
    this.fileRepository = fileRepository;
  }

  @ShellMethod(key = "transmit")
  public void transmit() throws IOException {
    System.out.println("Submissions to transmit....");

    List<UUID> submissionIds = new ArrayList<>();
    this.transmissionRepository.submissionsToTransmit(Sort.unsorted()).forEach(i -> {
      submissionIds.add(i.getId());
    });

    Map<String, Submission> appIdToSubmission = new HashMap<>();
    submissionIds.forEach(id -> {
      Submission submission = Submission.builder().id(id).build();
      Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
      if (transmission.getSubmittedToStateAt() == null) {
        appIdToSubmission.put(transmission.getApplicationNumber(), transmission.getSubmission());
      }
    });

    // initialize zip
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);

    String firstAppId = appIdToSubmission.keySet().stream().min(String::compareTo).get();
    String lastAppId = appIdToSubmission.keySet().stream().max(String::compareTo).get();

    //Format Apps__2023-07-05__100010-100300.zip
    try (FileOutputStream baos = new FileOutputStream("Apps__" + date + "__" + firstAppId + "-" + lastAppId + ".zip");
      ZipOutputStream zos = new ZipOutputStream(baos)) {

      appIdToSubmission.forEach((appNumber, submission) -> {

        String fileName = pdfService.generatePdfName(submission);
        Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
        String subfolder = transmission.getApplicationNumber() + "_" + submission.getInputData().get("lastName") + "/";
        try {
          // generate PDF
          byte[] file = pdfService.getFilledOutPDF(submission);

          zos.putNextEntry(new ZipEntry(subfolder));
          ZipEntry entry = new ZipEntry(subfolder + fileName);
          entry.setSize(file.length);
          zos.putNextEntry(entry);
          zos.write(file);
          zos.closeEntry();

          // Add uploaded docs
          List<UserFile> userFiles = transmissionRepository.userFilesBySubmission(submission);
          for (UserFile userFile : userFiles) {
            ZipEntry docEntry = new ZipEntry(subfolder + userFile.getOriginalName());
            docEntry.setSize(userFile.getFilesize().longValue());
            zos.putNextEntry(docEntry);

            CloudFile docFile = fileRepository.download(userFile.getRepositoryPath());
            byte[] bytes = new byte[Math.toIntExact(docFile.getFilesize())];
            try (FileInputStream fis = new FileInputStream(docFile.getFile())) {
              fis.read(bytes);
              zos.write(bytes);
            }

            zos.closeEntry();
          }

        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("Unable to write file, " + fileName);
        }
      });
    }

    // TODO send zip file

    // TODO update transmission in DB
  }
}
