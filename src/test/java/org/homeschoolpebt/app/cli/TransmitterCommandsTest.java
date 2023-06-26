package org.homeschoolpebt.app.cli;

import static org.assertj.core.util.DateUtil.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import formflow.library.data.UserFile;
import formflow.library.data.UserFileRepository;
import formflow.library.pdf.PdfService;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.upload.CloudFile;
import org.homeschoolpebt.app.upload.ReadOnlyCloudFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class TransmitterCommandsTest {

  @Autowired
  TransmitterCommands transmitterCommands;

  @Autowired
  SubmissionRepository submissionRepository;

  @Autowired
  TransmissionRepository transmissionRepository;

  @Autowired
  UserFileRepository userFileRepository;

  @MockBean
  PdfService pdfService;

  @MockBean
  ReadOnlyCloudFileRepository fileRepository;

  @MockBean
  SftpClient sftpClient;


  Submission submission;

  @BeforeEach
  void setup() {
    submission = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.ofEntries(
        Map.entry("firstName", "Tester"),
        Map.entry("lastName", "McTest")
      )).build();
    submissionRepository.save(submission);
    Transmission transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber("1001");
    transmissionRepository.save(transmission);

    var submission2 = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.ofEntries(
        Map.entry("firstName", "Other"),
        Map.entry("lastName", "McOtherson")
      )).build();
    submissionRepository.save(submission2);

    transmission = Transmission.fromSubmission(submission2);
    transmission.setConfirmationNumber("1002");
    transmissionRepository.save(transmission);

    UserFile docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(submission2);
    docfile.setOriginalName("originalFilename.png");
    userFileRepository.save(docfile);

    var submission3 = Submission.builder()
      .submittedAt(now())
      .flow("docUpload")
      .urlParams(new HashMap<>())
      .inputData(Map.ofEntries(
        Map.entry("firstName", "Tester"),
        Map.entry("lastName", "McTest")
      )).build();
    submissionRepository.save(submission3);

    transmission = Transmission.fromSubmission(submission3);
    transmission.setConfirmationNumber("1003");
    transmissionRepository.save(transmission);

    docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(submission3);
    docfile.setOriginalName("laterdoc.png");
    userFileRepository.save(docfile);
  }

  @Test
  void transmitZipFile() throws IOException, JSchException, SftpException {
    when(pdfService.getFilledOutPDF(any())).thenReturn("some bytes".getBytes());
    when(pdfService.generatePdfName(any())).thenReturn("applicant_summary.pdf");

    File docFile = new File("paystub.png");
    docFile.createNewFile();

    when(fileRepository.download(any())).thenReturn(new CloudFile(10L, docFile));

    transmitterCommands.transmit();

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    File zipFile = new File("Apps__" + date + "__1001-1003.zip");
    assertTrue(zipFile.exists());

    verify(sftpClient).uploadFile(zipFile.getName());

    Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
    assertNotNull(transmission.getSubmittedToStateAt());
    assertEquals(transmission.getSubmittedToStateFilename(), zipFile.getName());

    // cleanup
    zipFile.delete();
  }
}
