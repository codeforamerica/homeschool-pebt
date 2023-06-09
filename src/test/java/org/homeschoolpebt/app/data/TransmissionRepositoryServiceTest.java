package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class TransmissionRepositoryServiceTest {
  @Autowired
  private SubmissionRepositoryService submissionRepositoryService;

  @Autowired
  private TransmissionRepositoryService transmissionRepositoryService;

  @Test
  void createsTransmissionRecord() {
    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Avery Apple")
    )).submittedAt(new Date()).build();

    this.submissionRepositoryService.save(submission);
    var transmission = this.transmissionRepositoryService.createTransmissionRecord(submission);
    assertThat(transmission.getCreatedAt()).isNotNull();
    assertThat(transmission.getSubmission()).isEqualTo(submission);
    assertThat(transmission.getApplicationNumber()).startsWith("0010001");
    assertThat(transmission.getSubmittedToStateAt()).isNull();
  }

  @Test
  void setsApplicationNumberAutoIncrement() {
    var submission1 = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Avery Apple")
    )).submittedAt(new Date()).build();
    this.submissionRepositoryService.save(submission1);
    var transmission1 = this.transmissionRepositoryService.createTransmissionRecord(submission1);
    transmission1.setApplicationNumber("002123442");
    this.transmissionRepositoryService.save(transmission1);

    var submission2 = Submission.builder().inputData(Map.ofEntries(
      Map.entry("signature", "Billy Banana")
    )).submittedAt(new Date()).build();
    this.submissionRepositoryService.save(submission2);
    var transmission2 = this.transmissionRepositoryService.createTransmissionRecord(submission2);
    assertThat(transmission2.getApplicationNumber()).startsWith("0021235");
  }
}
