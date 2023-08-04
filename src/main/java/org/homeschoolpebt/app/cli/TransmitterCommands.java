package org.homeschoolpebt.app.cli;

import com.google.common.collect.Lists;
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
    // fix the batching to resolve the submissionAppIdsWithLaterDocs issue
    // adding a failed_at or something
    log.info("Finding submissions to transmit...");
    var all = this.transmissionRepository.submissionsToTransmit(Sort.unsorted());
    log.info("Total submissions to transmit in all batches is {}", all.size());
    var batches = Lists.partition(all, 200);
    for (var batch : batches) {
      transmitBatch(batch);
    }
    log.info("Finished transmission");
  }

  private void transmitBatch(List<Submission> batch) throws IOException, JSchException, SftpException {
    List<UUID> submissionIds = new ArrayList<>();
    batch.forEach(i -> {
      submissionIds.add(i.getId());
    });

    log.info("Transmitting batch of " + submissionIds.size() + " submissions");
    Set<String> submissionAppIdsWithLaterDocs = new HashSet<>();
    Map<String, Submission> appIdToSubmission = new HashMap<>();
    submissionIds.forEach(id -> {
      Transmission transmission = transmissionRepository.getTransmissionBySubmission(Submission.builder().id(id).build());
      Submission submission = transmission.getSubmission();
      appIdToSubmission.put(transmission.getConfirmationNumber(), submission);
      if ("docUpload".equals(submission.getFlow())) {
        submissionAppIdsWithLaterDocs.add((String) submission.getInputData().get("applicationNumber"));
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
    log.info("Finished transmission of a batch");
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
      for (var appNumberAndSubmission : appIdToSubmission.entrySet()) {
        var appNumber = appNumberAndSubmission.getKey();
        var submission = appNumberAndSubmission.getValue();

        Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
        if (transmission != null && (
          ("pebt".equals(submission.getFlow()) && doTransmitApplication(appIdsWithLaterDocs, appNumber, submission)) ||
            ("docUpload".equals(submission.getFlow()) && doTransmitDocUpload(submission)))
        ) {
          String subfolder = createSubfolderName(submission, transmission);
          try {
            if ("pebt".equals(submission.getFlow())) {
              // generate applicant summary
              byte[] file = pdfService.getFilledOutPDF(submission);
              String fileName = "00_" + pdfService.generatePdfName(submission);
              if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
              }

              zos.putNextEntry(new ZipEntry(subfolder));
              ZipEntry entry = new ZipEntry(subfolder + fileName);
              entry.setSize(file.length);
              zos.putNextEntry(entry);
              zos.write(file);
              zos.closeEntry();
            }

            // Add uploaded docs
            List<UserFile> userFiles = transmissionRepository.userFilesBySubmission(submission);
            int fileCount = 0;
            for (UserFile userFile : userFiles) {
              fileCount += 1;
              ZipEntry docEntry = new ZipEntry(subfolder + String.format("%02d", fileCount) + "_" + userFile.getOriginalName().replaceAll("[/:\\\\]", "_"));
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
            log.error("Unable to write file for appNumber, " + appNumber, e);
            throw e;
          }
        }
      }
    }

    return successfullySubmittedIds;
  }

  private boolean doTransmitDocUpload(Submission submission) {
    // Refuse to proceed if incomplete
    var inputData = submission.getInputData();
    if (inputData.get("firstName") == null || inputData.get("lastName") == null || inputData.get("applicationNumber") == null) {
      log.info("Declining to transmit incomplete doc upload submissionId={} -- firstName or lastName or applicationNumber is missing", submission.getId());
      return false;
    }
    return true;
  }

  private static boolean doTransmitApplication(Set<String> appIdsWithLaterDocs, String appNumber, Submission submission) {
    // Bail if the submission looks incomplete
    var inputData = submission.getInputData();
    if (inputData.get("hasMoreThanOneStudent") == null || inputData.get("firstName") == null || inputData.get("signature") == null) {
      log.info("Declining to transmit incomplete pebt app submissionId={} -- hasMoreThanOneStudent or firstName or signature is missing", submission.getId());
      return false;
    }

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
