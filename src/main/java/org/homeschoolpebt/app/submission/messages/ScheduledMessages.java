package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ScheduledMessages {
  MailgunEmailClient mailgunEmailClient;
  TwilioSmsClient twilioSmsClient;
  private final TransmissionRepository transmissionRepository;

  public ScheduledMessages(TransmissionRepository transmissionRepository) {
    this.transmissionRepository = transmissionRepository;
  }

  @Scheduled(cron = "0 0 19 * * *") // every day at 7pm GMT/Noon PDT
  void checkForUnsubmittedDocs() {
    Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
    List<Transmission> transmissions = transmissionRepository.findAll();
    transmissions.forEach(transmission -> {
      Submission submission = transmission.getSubmission();
      if (SubmissionUtilities.getMissingDocUploads(submission).size() == 0) {
        return;
      }

      // Only send a reminder if the submission was created 2 days ago, plus or minus 12 hours from now
      Date submissionCreatedAt = submission.getCreatedAt();
      long diffHours = ChronoUnit.HOURS.between(submissionCreatedAt.toInstant(), twoDaysAgo);
      if (diffHours < -12 || diffHours >= 12) {
        return;
      }

      var message = new ReminderMessage(submission, transmission);
      String emailAddress = (String) submission.getInputData().getOrDefault("email", "");

      if (!emailAddress.isBlank()) {
        var emailMessage = message.renderEmail();
        log.info("Sending email ReminderMessage for submission " + submission.getId());
        mailgunEmailClient.sendEmail(
          emailMessage.getSubject(),
          emailAddress,
          emailMessage.getBodyHtml()
        );
      } else {
        log.info("Not sending email ReminderMessage: no email address for submission " + submission.getId());
      }

      String phoneNumber = (String) submission.getInputData().getOrDefault("phoneNumber", "");
      if (!phoneNumber.isBlank()) {
        var smsMessage = message.renderSms();
        log.info("Sending SMS ReminderMessage for submission " + submission.getId());
        twilioSmsClient.sendMessage(phoneNumber, smsMessage.getBody());
      } else {
        log.info("Not sending SMS ReminderMessage: no phone number for submission " + submission.getId());
      }
    });
  }
}
