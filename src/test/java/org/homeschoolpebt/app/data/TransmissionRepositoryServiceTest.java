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
    assertThat(transmission.getSubmittedToStateAt()).isNull();
  }
}
