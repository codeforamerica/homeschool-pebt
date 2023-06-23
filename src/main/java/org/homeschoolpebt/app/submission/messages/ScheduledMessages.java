package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@ShellComponent
public class ScheduledMessages {
  MailgunEmailClient mailgunEmailClient;
  TwilioSmsClient twilioSmsClient;
  private final TransmissionRepository transmissionRepository;
  static final TemporalAdjuster REMINDER_TIME_FRAME = t -> t.minus(2, DAYS);

  public ScheduledMessages(TransmissionRepository transmissionRepository) {
    this.transmissionRepository = transmissionRepository;
  }

  @ShellMethod(key = "checkForUnsubmittedDocs")
  void checkForUnsubmittedDocs() {
    Instant reminderTime = Instant.now().with(REMINDER_TIME_FRAME);
    List<Transmission> transmissions = transmissionRepository.findAll();
    transmissions.stream()
      .filter(transmission -> SubmissionUtilities.getMissingDocUploads(transmission.getSubmission()).size() > 0)
      .filter(transmission -> isTimeToSendReminder(reminderTime, transmission.getSubmission().getCreatedAt().toInstant()))
      .forEach(this::sendDocReminderMessages);
  }

  static boolean isTimeToSendReminder(Instant reminderTime, Instant date) {
    long diffHours = ChronoUnit.HOURS.between(date, reminderTime);
    return diffHours > -12 && diffHours < 12;
  }

  private void sendDocReminderMessages(Transmission transmission) {
    Submission submission = transmission.getSubmission();
    var message = new DocReminderMessage(submission, transmission);

    String emailAddress = (String) submission.getInputData().getOrDefault("email", "");
    if (!emailAddress.isBlank()) {
      var emailMessage = message.renderEmail();
      log.info("Sending email %s for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
      mailgunEmailClient.sendEmail(emailMessage.getSubject(), emailAddress, emailMessage.getBodyHtml());
    } else {
      log.info("Not sending email %s: no email address for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
    }

    String phoneNumber = (String) submission.getInputData().getOrDefault("phoneNumber", "");
    if (!phoneNumber.isBlank()) {
      var smsMessage = message.renderSms();
      log.info("Sending SMS %s for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
      twilioSmsClient.sendMessage(phoneNumber, smsMessage.getBody());
    } else {
      log.info("Not sending SMS %s: no phone number for submission %s".formatted(message.getClass().getSimpleName(), submission.getId()));
    }
  }
}
