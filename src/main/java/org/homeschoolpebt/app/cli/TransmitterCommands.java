package org.homeschoolpebt.app.cli;

import static java.util.Collections.emptyList;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import formflow.library.data.Submission;
import formflow.library.data.UserFile;
import formflow.library.pdf.PdfService;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.upload.CloudFile;
import org.homeschoolpebt.app.upload.ReadOnlyCloudFileRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TransmitterCommands {

  public static final List<String> UPLOAD_DOCS = List.of("identityFiles", "enrollmentFiles", "incomeFiles", "unearnedIncomeFiles");

  public static final int SEVEN_DAYS_IN_MS = 7 * 24 * 60 * 60 * 1000;

  private final TransmissionRepository transmissionRepository;
  private final PdfService pdfService;

  private final ReadOnlyCloudFileRepository fileRepository;

  private final SftpClient sftpClient;

  public TransmitterCommands(TransmissionRepository transmissionRepository,
    PdfService pdfService, ReadOnlyCloudFileRepository fileRepository, SftpClient sftpClient) {
    this.transmissionRepository = transmissionRepository;
    this.pdfService = pdfService;
    this.fileRepository = fileRepository;
    this.sftpClient = sftpClient;
  }

  @ShellMethod(key = "transmit")
  public void transmit() throws IOException, JSchException, SftpException {
    System.out.println("Submissions to transmit....");

    List<UUID> submissionIds = new ArrayList<>();
    this.transmissionRepository.submissionsToTransmit(Sort.unsorted()).forEach(i -> {
      submissionIds.add(i.getId());
    });

    Map<String, List<String>> appIdToLaterDoc = new HashMap<>();
    Map<String, Submission> appIdToSubmission = new HashMap<>();
    submissionIds.forEach(id -> {
      Transmission transmission = transmissionRepository.getTransmissionBySubmission(Submission.builder().id(id).build());
      Submission submission = transmission.getSubmission();
      if (transmission.getSubmittedToStateAt() == null) {
        appIdToSubmission.put(transmission.getConfirmationNumber(), submission);
        if ("docUpload".equals(submission.getFlow())) {
          String appId = (String) submission.getInputData().get("applicationNumber");
          List<String> confirmationNumbers = appIdToLaterDoc.computeIfAbsent(appId, k -> new ArrayList<>());
          confirmationNumbers.add(transmission.getConfirmationNumber());
        }
      }
    });

    String zipFilename = createZipFilename(appIdToSubmission);
    zipFiles(appIdToSubmission, zipFilename, appIdToLaterDoc);

    // send zip file
    sftpClient.uploadFile(zipFilename);

    // Update transmission in DB
    submissionIds.forEach(id -> {
      Submission submission = Submission.builder().id(id).build();
      Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
      transmission.setSubmittedToStateAt(new Date());
      transmission.setSubmittedToStateFilename(zipFilename);
      transmissionRepository.save(transmission);
    });
  }

  @NotNull
  private static String createZipFilename(Map<String, Submission> appIdToSubmission) {
    // Format: Apps__2023-07-05__100010-100300.zip
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);

    String firstAppId = appIdToSubmission.keySet().stream().min(String::compareTo).get();
    String lastAppId = appIdToSubmission.keySet().stream().max(String::compareTo).get();

    return "Apps__" + date + "__" + firstAppId + "-" + lastAppId + ".zip";
  }

  private void zipFiles(Map<String, Submission> appIdToSubmission, String zipFileName, Map<String, List<String>> appIdToLaterDoc) throws IOException {
    try (FileOutputStream baos = new FileOutputStream(zipFileName);
      ZipOutputStream zos = new ZipOutputStream(baos)) {

      appIdToSubmission.forEach((appNumber, submission) -> {

        Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
        String subfolder = createSubfolderName(submission, transmission);
        try {
          if ("pebt".equals(submission.getFlow())) {
            // delay 7 days if there aren't any uploaded docs associated with this application
            Date submittedAt = submission.getSubmittedAt();
            Date sevenDaysAgo = new Date(System.currentTimeMillis() - SEVEN_DAYS_IN_MS);
            boolean hasLaterDocs = appIdToLaterDoc.containsKey(appNumber);

            Map<String, Object> inputData = submission.getInputData();
            if (submittedAt.before(sevenDaysAgo) || hasLaterDocs || UPLOAD_DOCS.stream().anyMatch(k -> inputData.get(k) != null && !inputData.get(k).equals(emptyList()))) {
              // generate applicant summary
              byte[] file = pdfService.getFilledOutPDF(submission);

              String fileName = pdfService.generatePdfName(submission);
              zos.putNextEntry(new ZipEntry(subfolder));
              ZipEntry entry = new ZipEntry(subfolder + fileName);
              entry.setSize(file.length);
              zos.putNextEntry(entry);
              zos.write(file);
              zos.closeEntry();
            }
          }

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
          System.out.println("Unable to write file for appNumber, " + appNumber);
        }
      });
    }
  }

  @NotNull
  private static String createSubfolderName(Submission submission, Transmission transmission) {
    Map<String, Object> inputData = submission.getInputData();
    if ("pebt".equals(submission.getFlow())) {
      return transmission.getConfirmationNumber() + "_" + inputData.get("lastName") + "/";
    } else {
      return "LaterDoc_" + inputData.get("applicationNumber") + "_" + inputData.get("lastName") + "_" + inputData.get("firstName") + "/";
    }
  }
}
