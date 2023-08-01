package org.homeschoolpebt.app.cli;

import static org.assertj.core.util.DateUtil.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
      .inputData(Map.of(
        "firstName", "Tester",
        "lastName", "McTest"
      )).build();
    submissionRepository.save(submission);
    Transmission transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber("1001");
    transmissionRepository.save(transmission);

    var submissionWithDocs = Submission.builder()
      .submittedAt(now())
      .flow("pebt")
      .urlParams(new HashMap<>())
      .inputData(Map.of(
        "firstName", "Other",
        "lastName", "McOtherson",
        "identityFiles", List.of("some-file-id")
      )).build();
    submissionRepository.save(submissionWithDocs);

    transmission = Transmission.fromSubmission(submissionWithDocs);
    transmission.setConfirmationNumber("1002");
    transmissionRepository.save(transmission);

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
    docfileWeirdFilename.setOriginalName("weird/:filename.jpg");
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
    transmission.setConfirmationNumber("1003");
    transmissionRepository.save(transmission);

    docfile = new UserFile();
    docfile.setFilesize(10.0f);
    docfile.setSubmission_id(docUploadOnly);
    docfile.setOriginalName("laterdoc.png");
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
    transmission.setConfirmationNumber("1004");
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
    File zipFile = new File("Apps__" + date + "__1001-1004.zip");
    assertTrue(zipFile.exists());

    verify(sftpClient).uploadFile(zipFile.getName());

    Transmission transmission = transmissionRepository.getTransmissionBySubmission(submission);
    assertNotNull(transmission.getSubmittedToStateAt());
    assertEquals(transmission.getSubmittedToStateFilename(), zipFile.getName());

    String destDir = "output";
    List<String> fileNames = unzip(zipFile.getPath(), destDir);

    assertEquals(8, fileNames.size());
    assertThat(fileNames, hasItem("output/1001_McTest/"));
    assertThat(fileNames, hasItem("output/1001_McTest/00_applicant_summary.pdf"));
    assertThat(fileNames, hasItem("output/LaterDoc_1001_McTest_Tester/01_laterdoc.png"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/00_applicant_summary.pdf"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/01_originalFilename.png"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/02_originalFilename.png"));
    assertThat(fileNames, hasItem("output/1002_McOtherson/03_weird__filename.jpg"));

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
