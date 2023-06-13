package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.submission.messages.ConfirmationMessage;
import org.homeschoolpebt.app.submission.messages.TwilioSmsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HandleApplicationSigned implements Action {
  @Autowired
  MailgunEmailClient mailgunEmailClient;
  @Autowired
  TwilioSmsClient twilioSmsClient;
  @Autowired
  private TransmissionRepositoryService transmissionRepositoryService;

  public void run(Submission submission) {
    var transmission = transmissionRepositoryService.createTransmissionRecord(submission);

    var message = new ConfirmationMessage(submission, transmission);
    String emailAddress = (String) submission.getInputData().get("email");

    if (emailAddress != null && !emailAddress.isBlank()) {
      var emailMessage = message.renderEmail();
      log.info("Sending email ConfirmationMessage to " + emailAddress);
      mailgunEmailClient.sendEmail(
        emailMessage.getSubject(),
        emailAddress,
        emailMessage.getBodyHtml()
      );
    } else {
      log.info("Not sending email ConfirmationMessage: no email address for submission " + submission.getId());
    }

    String phoneNumber = (String) submission.getInputData().get("phoneNumber");
    if (phoneNumber != null && !phoneNumber.isBlank()) {
      var smsMessage = message.renderSms();
      log.info("Sending SMS ConfirmationMessage to " + phoneNumber);
      twilioSmsClient.sendMessage(phoneNumber, smsMessage.getBody());
    } else {
      log.info("Not sending SMS ConfirmationMessage: no phone number for submission " + submission.getId());
    }
  }
}
