package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.SentMessage;
import org.homeschoolpebt.app.data.SentMessageRepositoryService;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.Date;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@ShellComponent
public class ScheduledMessages {
  private final MailgunEmailClient mailgunEmailClient;
  private final TwilioSmsClient twilioSmsClient;
  private final TransmissionRepository transmissionRepository;
  private final SentMessageRepositoryService sentMessageRepositoryService;
  static final List<TemporalAdjuster> REMINDER_TIME_FRAMES = List.of(
    t -> t.minus(2, DAYS),
    t -> t.minus(4, DAYS));

  public ScheduledMessages(MailgunEmailClient mailgunEmailClient, TwilioSmsClient twilioSmsClient, TransmissionRepository transmissionRepository, SentMessageRepositoryService sentMessageRepositoryService) {
    this.mailgunEmailClient = mailgunEmailClient;
    this.twilioSmsClient = twilioSmsClient;
    this.transmissionRepository = transmissionRepository;
    this.sentMessageRepositoryService = sentMessageRepositoryService;
  }

  @ShellMethod(key = "checkForUnsubmittedDocs")
  void checkForUnsubmittedDocs() {
    List<Instant> reminderTimes = REMINDER_TIME_FRAMES.stream().map(Instant.now()::with).toList();
    List<Transmission> transmissions = transmissionRepository.findAll();
    log.info("Looking for submissions to send reminders");
    transmissions.stream()
      .filter(transmission -> SubmissionUtilities.getMissingDocUploads(transmission.getSubmission()).size() > 0)
      .filter(transmission -> isTimeToSendReminder(reminderTimes, transmission.getSubmission().getCreatedAt().toInstant()))
      .forEach(this::sendDocReminderMessages);
    log.info("sendDocReminderMessages completed");
  }

  static boolean isTimeToSendReminder(List<Instant> reminderTimes, Instant date) {
    return reminderTimes.stream().anyMatch(reminderTime -> {
      long diffHours = ChronoUnit.HOURS.between(date, reminderTime);
      return diffHours > -12 && diffHours < 12;
    });
  }

  private void sendDocReminderMessages(Transmission transmission) {
    Submission submission = transmission.getSubmission();
    var message = new DocReminderMessage(submission, transmission);

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
