package org.homeschoolpebt.app.cli;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import formflow.library.data.Submission;
import formflow.library.data.UserFile;
import formflow.library.pdf.PdfService;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.upload.CloudFile;
import org.homeschoolpebt.app.upload.ReadOnlyCloudFileRepository;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@Slf4j
@ShellComponent
public class TransmitterCommands {

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
    log.info("Finding submissions to transmit...");

    List<UUID> submissionIds = new ArrayList<>();
    this.transmissionRepository.submissionsToTransmit(Sort.unsorted()).forEach(i -> {
      submissionIds.add(i.getId());
    });

    log.info("Transmitting " + submissionIds.size() + " submissions");
    Set<String> submissionAppIdsWithLaterDocs = new HashSet<>();
    Map<String, Submission> appIdToSubmission = new HashMap<>();
    submissionIds.forEach(id -> {
      Transmission transmission = transmissionRepository.getTransmissionBySubmission(Submission.builder().id(id).build());
      Submission submission = transmission.getSubmission();
      if (transmission.getSubmittedToStateAt() == null) {
        appIdToSubmission.put(transmission.getConfirmationNumber(), submission);
        if ("docUpload".equals(submission.getFlow())) {
          submissionAppIdsWithLaterDocs.add((String) submission.getInputData().get("applicationNumber"));
        }
      }
    });

    String zipFilename = createZipFilename(appIdToSubmission);
    List<UUID> successfullySubmittedIds = zipFiles(appIdToSubmission, zipFilename, submissionAppIdsWithLaterDocs);

    // send zip file
    log.info("Uploading zip file");
    sftpClient.uploadFile(zipFilename);

    // Update transmission in DB
    successfullySubmittedIds.forEach(id -> {
      Submission submission = Submission.builder().id(id).build();
      Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
      transmission.setSubmittedToStateAt(new Date());
      transmission.setSubmittedToStateFilename(zipFilename);
      transmissionRepository.save(transmission);
    });
    log.info("Finished transmission");
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

  private List<UUID> zipFiles(Map<String, Submission> appIdToSubmission, String zipFileName, Set<String> appIdsWithLaterDocs) throws IOException {
    List<UUID> successfullySubmittedIds = new ArrayList<>();
    try (FileOutputStream baos = new FileOutputStream(zipFileName);
      ZipOutputStream zos = new ZipOutputStream(baos)) {
      appIdToSubmission.forEach((appNumber, submission) -> {
        Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
        if (transmission != null) {
          String subfolder = createSubfolderName(submission, transmission);
          try {
            if ("pebt".equals(submission.getFlow()) && doTransmitApplication(appIdsWithLaterDocs, appNumber, submission)) {
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
            successfullySubmittedIds.add(submission.getId());
          } catch (IOException e) {
            log.error("Unable to write file for appNumber, " + appNumber);
          }
        }
      });
    }
    return successfullySubmittedIds;
  }

  private static boolean doTransmitApplication(Set<String> appIdsWithLaterDocs, String appNumber, Submission submission) {
    // delay 7 days if there aren't any uploaded docs associated with this application
    Instant submittedAt = submission.getSubmittedAt().toInstant();
    long diffDays = ChronoUnit.DAYS.between(submittedAt, Instant.now());
    boolean submitted7daysAgo = Math.abs(diffDays) >= 7;
    boolean hasLaterDocs = appIdsWithLaterDocs.contains(appNumber);
    List<String> missingDocUploads = SubmissionUtilities.getMissingDocUploads(submission);
    boolean hasUploadedDocs = missingDocUploads.isEmpty();
    return submitted7daysAgo || hasLaterDocs || hasUploadedDocs;
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
