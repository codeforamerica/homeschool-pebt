package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class TransmissionReposItoryServiceTest {
  @Autowired
  private SubmissionRepositoryService submissionRepositoryService;

  @Autowired
  private TransmissionRepositoryService transmissionRepositoryService;

  @Test
  void createsTransmissionRecord() {
    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Avery Apple")
    )).flow("pebt").submittedAt(new Date()).build();

    this.submissionRepositoryService.save(submission);
    var transmission = this.transmissionRepositoryService.createTransmissionRecord(submission);
    assertThat(transmission.getFlow()).isEqualTo("pebt");
    assertThat(transmission.getCreatedAt()).isNotNull();
    assertThat(transmission.getSubmission()).isEqualTo(submission);
    assertThat(transmission.getConfirmationNumber()).startsWith("0010001");
    assertThat(transmission.getSubmittedToStateAt()).isNull();
  }

  @Test
  void setsApplicationNumberAutoIncrement() {
    var submission1 = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Avery Apple")
    )).flow("pebt").submittedAt(new Date()).build();
    this.submissionRepositoryService.save(submission1);
    var transmission1 = this.transmissionRepositoryService.createTransmissionRecord(submission1);
    transmission1.setConfirmationNumber("002123442");
    this.transmissionRepositoryService.save(transmission1);

    var submission2 = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Billy Banana")
    )).flow("pebt").submittedAt(new Date()).build();
    this.submissionRepositoryService.save(submission2);
    var transmission2 = this.transmissionRepositoryService.createTransmissionRecord(submission2);
    assertThat(transmission2.getConfirmationNumber()).startsWith("0021235");
  }

  @Test
  void testCreateLaterdocTransmissionRecord() {
    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Avery Apple")
    )).flow("docUpload").submittedAt(new Date()).build();

    this.submissionRepositoryService.save(submission);
    var transmission = this.transmissionRepositoryService.createLaterdocTransmissionRecord(submission);
    assertThat(transmission.getFlow()).isEqualTo("docUpload");
    assertThat(transmission.getCreatedAt()).isNotNull();
    assertThat(transmission.getSubmission()).isEqualTo(submission);
    assertThat(transmission.getConfirmationNumber()).matches(Pattern.compile("0010001\\d\\dL"));
    assertThat(transmission.getSubmittedToStateAt()).isNull();
  }
}
