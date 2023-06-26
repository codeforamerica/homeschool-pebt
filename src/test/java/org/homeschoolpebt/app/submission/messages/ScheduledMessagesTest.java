package org.homeschoolpebt.app.submission.messages;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.*;
import static org.homeschoolpebt.app.submission.messages.ScheduledMessages.REMINDER_TIME_FRAMES;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScheduledMessagesTest {
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
}
