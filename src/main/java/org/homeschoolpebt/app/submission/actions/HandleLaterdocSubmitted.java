package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HandleLaterdocSubmitted implements Action {
  @Autowired
  MailgunEmailClient mailgunEmailClient;

  @Autowired
  private TransmissionRepositoryService transmissionRepositoryService;

  public void run(Submission submission) {
    var transmission = transmissionRepositoryService.createLaterdocTransmissionRecord(submission);

    // send laterdoc receipt confirmation message
  }
}
