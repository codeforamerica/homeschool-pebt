package org.homeschoolpebt.app.submission.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailgun.model.message.MessageResponse;
import com.twilio.rest.api.v2010.account.Message;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import org.homeschoolpebt.app.data.SentMessageRepositoryService;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.*;
import static org.homeschoolpebt.app.submission.messages.ScheduledMessages.REMINDER_TIME_FRAMES;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringBootTest
public class ScheduledMessagesTest {
  @Autowired
  ScheduledMessages scheduledMessages;
  @MockBean
  TransmissionRepository transmissionRepository;
  @MockBean
  SubmissionUtilities submissionUtilities;
  @MockBean
  MailgunEmailClient mailgunEmailClient;
  @MockBean
  TwilioSmsClient twilioSmsClient;
  @MockBean
  SentMessageRepositoryService sentMessageRepositoryService;

  private static final Instant NOW = Instant.now().atZone(ZoneOffset.UTC)
    .with(ChronoField.HOUR_OF_DAY, 12)
    .with(ChronoField.MINUTE_OF_HOUR, 0)
    .with(ChronoField.SECOND_OF_MINUTE, 0)
    .toInstant();

  private static Stream<TemporalAdjuster> provideReminderTimeFrames() {
    return REMINDER_TIME_FRAMES.stream();
  }

  @ParameterizedTest
  @MethodSource("provideReminderTimeFrames")
  void testReturnTrueWhenItIsTimeToSendAReminderSubmittedAfterMidnightTwoDaysEarlier(TemporalAdjuster temporalAdjuster) {
    assertTrue(ScheduledMessages.isTimeToSendReminder(
      REMINDER_TIME_FRAMES.stream().map(NOW::with).toList(),
      NOW.with(temporalAdjuster).minus(12, HOURS).plusSeconds(1))); // 00:00:01
  }

  @ParameterizedTest
  @MethodSource("provideReminderTimeFrames")
  void testReturnTrueWhenItIsTimeToSendAReminderSubmittedBeforeMidnightTwoDaysEarlier(TemporalAdjuster temporalAdjuster) {
    assertTrue(ScheduledMessages.isTimeToSendReminder(
      REMINDER_TIME_FRAMES.stream().map(NOW::with).toList(),
      NOW.with(temporalAdjuster).plus(12, HOURS).minusSeconds(1))); // 23:59:59
  }

  @ParameterizedTest
  @MethodSource("provideReminderTimeFrames")
  void testReturnFalseWhenItIsTooLateToSendAReminderSubmittedBeforeMidnightThreeDaysBefore(TemporalAdjuster temporalAdjuster) {
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      REMINDER_TIME_FRAMES.stream().map(NOW::with).toList(),
      NOW.with(temporalAdjuster).minus(12, HOURS).minusSeconds(1))); // 23:59:59
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      REMINDER_TIME_FRAMES.stream().map(NOW::with).toList(),
      NOW.with(temporalAdjuster).minus(12, HOURS))); // 00:00:00
  }

  @ParameterizedTest
  @MethodSource("provideReminderTimeFrames")
  void testReturnFalseWhenItIsTooSoonToSendAReminderSubmittedAfterMidnightOneDayBefore(TemporalAdjuster temporalAdjuster) {
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      REMINDER_TIME_FRAMES.stream().map(NOW::with).toList(),
      NOW.with(temporalAdjuster).plus(12, HOURS).plusSeconds(1))); // 00:00:01
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      REMINDER_TIME_FRAMES.stream().map(NOW::with).toList(),
      NOW.with(temporalAdjuster).plus(12, HOURS))); // 00:00:00
  }

  @Test
  void testCheckForUnsubmittedDocs_nullTranmissions() {
    Mockito.when(transmissionRepository.findAll()).thenReturn(null);
    scheduledMessages.checkForUnsubmittedDocs();
    Mockito.verify(mailgunEmailClient, Mockito.never()).sendEmail(any(), any(), any());
    Mockito.verify(twilioSmsClient, Mockito.never()).sendMessage(any(), any());
    Mockito.verify(sentMessageRepositoryService, Mockito.never()).save(any());
  }

  @Test
  void testCheckForUnsubmittedDocs_emptyListTranmissions() {
    Mockito.when(transmissionRepository.findAll()).thenReturn(Collections.emptyList());
    scheduledMessages.checkForUnsubmittedDocs();
    Mockito.verify(mailgunEmailClient, Mockito.never()).sendEmail(any(), any(), any());
    Mockito.verify(twilioSmsClient, Mockito.never()).sendMessage(any(), any());
    Mockito.verify(sentMessageRepositoryService, Mockito.never()).save(any());
  }

  @Test
  void testCheckForUnsubmittedDocs_singleTranmissionWithMissingDoc() {
    Transmission dummyTransmission = Transmission.fromSubmission(Submission.builder()
      .inputData(Map.of(
        "email", "test@email.com",
        "phoneNumber", "1111111111"
      ))
      .createdAt(Date.from(Instant.now().minus(2, DAYS)))
      .build());
    dummyTransmission.setConfirmationNumber("anything");
    Mockito.when(transmissionRepository.findAll()).thenReturn(Collections.singletonList(dummyTransmission));
    Mockito.when(mailgunEmailClient.sendEmail(
      "Documents Needed for P-EBT 4.0 Application", "test@email.com", "<html>\n"
          + "  <body>\n"
          + "    <p>Dear  ,</p>\n"
          + "    <p>Thank you for beginning the application for P-EBT benefits. This is a reminder to upload the documents for your application. \n"
          + "    You will need proof of income, a student  ID and virtual school documentation for each student, if applicable.</p>\n"
          + "    <p>You can find the link here: https://www.getpebt.org/docs</p>\n"
          + "    <p>Your application number is anything.</p>\n"
          + "    <p>If you need food now, you can contact your local food bank at https://www.cafoodbanks.org/find-food. You can also apply for CalFresh at https://www.getcalfresh.org.</p>\n"
          + "    <p>- California Department of Social Services</p>\n"
          + "  </body>\n"
          + "</html>\n"))
      .thenReturn(MessageResponse.builder().id("id").build());
    Mockito.when(twilioSmsClient.sendMessage("1111111111", "Thank you for beginning the application for P-EBT benefits. This is a reminder to upload the documents for your application. You will need to upload proof of identity for each student, proof of income, and virtual school documentation for each student, if applicable. \n"
      + "Your application number is anything and you can find the link here: https://www.getpebt.org/docs\n")).thenReturn(Message.fromJson("{\"sid\": \"sid\"}", new ObjectMapper()));

    scheduledMessages.checkForUnsubmittedDocs();
    Mockito.verify(mailgunEmailClient, Mockito.times(1)).sendEmail(any(), any(), any());
    Mockito.verify(twilioSmsClient, Mockito.times(1)).sendMessage(any(), any());
    Mockito.verify(sentMessageRepositoryService, Mockito.times(2)).save(any());
  }

  @Test
  void testCheckForUnsubmittedDocs_singleTranmissionNoMissingDoc() {
    Transmission dummyTransmission = Transmission.fromSubmission(Submission.builder()
      .inputData(Map.of(
        "email", "test@email.com",
        "phoneNumber", "1111111111",
        "identityFiles", "something",
        "enrollmentFiles", "something",
        "incomeFiles", "something",
        "unearnedIncomeFiles", "something"
      ))
      .createdAt(Date.from(Instant.now().minus(2, DAYS)))
      .build());
    dummyTransmission.setConfirmationNumber("anything");
    Mockito.when(transmissionRepository.findAll()).thenReturn(Collections.singletonList(dummyTransmission));

    scheduledMessages.checkForUnsubmittedDocs();
    Mockito.verify(mailgunEmailClient, Mockito.never()).sendEmail(any(), any(), any());
    Mockito.verify(twilioSmsClient, Mockito.never()).sendMessage(any(), any());
    Mockito.verify(sentMessageRepositoryService, Mockito.never()).save(any());
  }
}
