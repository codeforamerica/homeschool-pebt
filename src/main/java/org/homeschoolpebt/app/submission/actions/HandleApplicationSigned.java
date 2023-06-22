package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.submission.messages.ConfirmationMessage;
import org.homeschoolpebt.app.submission.messages.DocReminderMessage;
import org.homeschoolpebt.app.submission.messages.PebtMessage;
import org.homeschoolpebt.app.submission.messages.TwilioSmsClient;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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

    PebtMessage message;
    if (SubmissionUtilities.getMissingDocUploads(submission).size() == 0) {
      message = new ConfirmationMessage(submission, transmission);
    } else {
      message = new DocReminderMessage(submission, transmission);
    }

    String emailAddress = (String) submission.getInputData().getOrDefault("email", "");
    if (!emailAddress.isBlank()) {
      var emailMessage = message.renderEmail();
      log.info("Sending email PebtMessage for submission " + submission.getId());
      mailgunEmailClient.sendEmail(emailMessage.getSubject(), emailAddress, emailMessage.getBodyHtml());
    } else {
      log.info("Not sending email PebtMessage: no email address for submission " + submission.getId());
    }

    String phoneNumber = (String) submission.getInputData().getOrDefault("phoneNumber", "");
    if (!phoneNumber.isBlank()) {
      var smsMessage = message.renderSms();
      log.info("Sending SMS ConfirmationMessage for submission " + submission.getId());
      twilioSmsClient.sendMessage(phoneNumber, smsMessage.getBody());
    } else {
      log.info("Not sending SMS PebtMessage: no phone number for submission " + submission.getId());
    }
  }
}
