package org.homeschoolpebt.app.cli;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import formflow.library.data.UserFile;
import formflow.library.data.UserFileRepository;
import formflow.library.pdf.PdfService;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.util.DateUtil.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    int transmissionId = 1001;
    submission = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        "firstName", "Tester",
        "lastName", "McTest",
        "hasMoreThanOneStudent", "false",
        "signature", "Tester McTest sig"
      )).build();
    submissionRepository.save(submission);
    Transmission transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber(String.format("%d", transmissionId));
    transmissionRepository.save(transmission);
    transmissionId++;

    var submissionWithDocs = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        "firstName", "Other",
        "lastName", "McOtherson",
        "signature", "Other McOtherson sig",
        "hasMoreThanOneStudent", "false",
        "identityFiles", "[\"some-file-id\"]"
      )).build();
    submissionRepository.save(submissionWithDocs);

    transmission = Transmission.fromSubmission(submissionWithDocs);
    transmission.setConfirmationNumber(String.format("%d", transmissionId));
    transmissionRepository.save(transmission);
    transmissionId++;

    UserFile docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(submissionWithDocs);
    docfile.setOriginalName("originalFilename.png");
    userFileRepository.save(docfile);

    UserFile docfileSameName = new UserFile();
    docfileSameName.setFilesize(10.0f);
    docfileSameName.setSubmission_id(submissionWithDocs);
    docfileSameName.setOriginalName("originalFilename.png");
    userFileRepository.save(docfileSameName);

    UserFile docfileWeirdFilename = new UserFile();
    docfileWeirdFilename.setFilesize(10.0f);
    docfileWeirdFilename.setSubmission_id(submissionWithDocs);
    docfileWeirdFilename.setOriginalName("weird/:\\filename.jpg");
    userFileRepository.save(docfileWeirdFilename);

    var docUploadOnly = Submission.builder()
      .submittedAt(now())
      .flow("docUpload")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        "firstName", "Tester",
        "lastName", "McTest",
        "applicationNumber", "1001"
      )).build();
    submissionRepository.save(docUploadOnly);

    transmission = Transmission.fromSubmission(docUploadOnly);
    transmission.setConfirmationNumber(String.format("%d", transmissionId));
    transmissionRepository.save(transmission);
    transmissionId++;

    docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(docUploadOnly);
    docfile.setOriginalName("laterdoc.png");
    userFileRepository.save(docfile);

    var incompleteDocUpload = Submission.builder()
      .submittedAt(now())
      .flow("docUpload")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        // firstName somehow missing
        "lastName", "McIncompleteLaterDoc",
        "applicationNumber", "1001"
      )).build();
    submissionRepository.save(incompleteDocUpload);
    transmission = Transmission.fromSubmission(incompleteDocUpload);
    transmission.setConfirmationNumber(String.format("%d", transmissionId));
    transmissionRepository.save(transmission);
    transmissionId++;

    docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(incompleteDocUpload);
    docfile.setOriginalName("laterdoc.png");
    userFileRepository.save(docfile);

    var submissionWithoutSignature = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        "firstName", "Sigless",
        "lastName", "McSigless",
        "hasMoreThanOneStudent", "false",
        "identityFiles", "[\"some-file-id\"]"
      )).build();
    submissionRepository.save(submissionWithoutSignature);
    transmission = Transmission.fromSubmission(submissionWithoutSignature);
    transmission.setConfirmationNumber(String.format("%d", transmissionId));
    transmissionRepository.save(transmission);
    transmissionId++;

    docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(submissionWithoutSignature);
    docfile.setOriginalName("originalFilename.png");
    userFileRepository.save(docfile);

    var submissionWithoutDocs = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        "firstName", "Testing",
        "lastName", "McTesting"
      )).build();
    submissionRepository.save(submissionWithoutDocs);
    transmission = Transmission.fromSubmission(submissionWithoutDocs);
    transmission.setConfirmationNumber(String.format("%d", transmissionId));
    transmissionRepository.save(transmission);
  }

  @Test
  void transmitZipFile() throws IOException, JSchException, SftpException {
    when(pdfService.getFilledOutPDF(any())).thenReturn("some bytes".getBytes());
    when(pdfService.generatePdfName(any())).thenReturn("applicant_summary");

    File docFile = new File("paystub.png");
    docFile.createNewFile();

    when(fileRepository.download(any())).thenReturn(new CloudFile(10L, docFile));

    transmitterCommands.transmit();

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    File zipFile = new File("Apps__" + date + "__1001-1006.zip");
    assertTrue(zipFile.exists());

    verify(sftpClient).uploadFile(zipFile.getName());

    Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
    assertNotNull(transmission.getSubmittedToStateAt());
    assertEquals(transmission.getSubmittedToStateFilename(), zipFile.getName());

    String destDir = "output";
    List<String> fileNames = unzip(zipFile.getPath(), destDir);

    assertThat(fileNames, hasItem("output/1001_McTest/"));
    assertThat(fileNames, hasItem("output/1001_McTest/00_applicant_summary.pdf"));
    assertThat(fileNames, hasItem("output/LaterDoc_1001_McTest_Tester/01_laterdoc.png"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/00_applicant_summary.pdf"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/01_originalFilename.png"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/02_originalFilename.png"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/03_weird___filename.jpg"));
    assertThat(fileNames, not(hasItem("output/1005_McSigless/01_originalFilename.png")));
    assertThat(fileNames, not(hasItem("output/LaterDoc_1001_McIncompleteLaterDoc_null/01_laterdoc.png")));
    assertEquals(8, fileNames.size());


    // cleanup
    zipFile.delete();
    docFile.delete();
  }

  private static List<String> unzip(String zipFilePath, String destDir) {
    List<String> result = new ArrayList<>();
    //buffer for read and write data to file
    byte[] buffer = new byte[1024];
    try (FileInputStream fis = new FileInputStream(zipFilePath)) {
      ZipInputStream zis = new ZipInputStream(fis);
      ZipEntry ze = zis.getNextEntry();
      while (ze != null) {
        String fileName = ze.getName();
        result.add(destDir + File.separator + fileName);
        while (zis.read(buffer) > 0) {
        }
        zis.closeEntry();
        ze = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}
