package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.submission.messages.ConfirmationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HandleApplicationSigned implements Action {
  @Autowired
  MailgunEmailClient mailgunEmailClient;

  @Autowired
  private TransmissionRepositoryService transmissionRepositoryService;

  public void run(Submission submission) {
    var transmission = transmissionRepositoryService.createTransmissionRecord(submission);

    var message = new ConfirmationMessage(submission, transmission);
    var emailMessage = message.renderEmail();
    var emailAddress = submission.getInputData().get("email");

    if (emailAddress != null) {
      log.info("Sending ConfirmationMessage to " + emailAddress);
      mailgunEmailClient.sendEmail(
        emailMessage.getSubject(),
        (String) emailAddress,
        emailMessage.getBodyHtml()
      );
    } else {
      log.info("Not sending ConfirmationMessage: no email address for submission " + submission.getId());
    }
  }
}
