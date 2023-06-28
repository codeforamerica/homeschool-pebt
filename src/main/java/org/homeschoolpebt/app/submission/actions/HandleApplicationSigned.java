package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.SentMessage;
import org.homeschoolpebt.app.data.SentMessageRepositoryService;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.submission.messages.ConfirmationMessage;
import org.homeschoolpebt.app.submission.messages.DocReminderMessage;
import org.homeschoolpebt.app.submission.messages.PebtMessage;
import org.homeschoolpebt.app.submission.messages.TwilioSmsClient;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class HandleApplicationSigned implements Action {
  @Autowired
  MailgunEmailClient mailgunEmailClient;
  @Autowired
  TwilioSmsClient twilioSmsClient;
  @Autowired
  private TransmissionRepositoryService transmissionRepositoryService;
  @Autowired
  SentMessageRepositoryService sentMessageRepositoryService;

  public void run(Submission submission) {
    if (transmissionRepositoryService.transmissionExists(submission)) {
      // already submitted. don't do anything again.
      return;
    }

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
      log.info("Sending email %s for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
      var mailgunResponse = mailgunEmailClient.sendEmail(emailMessage.getSubject(), emailAddress, emailMessage.getBodyHtml());
      sentMessageRepositoryService.save(
        SentMessage.builder()
          .submission(submission)
          .messageName(message.getClass().getSimpleName())
          .sentAt(new Date())
          .provider("mailgun")
          .providerMessageId(mailgunResponse.getId())
          .build()
      );
    } else {
      log.info("Not sending email %s: no email address for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
    }

    String phoneNumber = (String) submission.getInputData().getOrDefault("phoneNumber", "");
    if (!phoneNumber.isBlank()) {
      var smsMessage = message.renderSms();
      log.info("Sending SMS %s for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
      var twilioResponse = twilioSmsClient.sendMessage(phoneNumber, smsMessage.getBody());
      sentMessageRepositoryService.save(
        SentMessage.builder()
          .submission(submission)
          .messageName(message.getClass().getSimpleName())
          .sentAt(new Date())
          .provider("twilio")
          .providerMessageId(twilioResponse.getSid())
          .build()
      );
    } else {
      log.info("Not sending SMS %s: no phone number for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
    }
  }
}
