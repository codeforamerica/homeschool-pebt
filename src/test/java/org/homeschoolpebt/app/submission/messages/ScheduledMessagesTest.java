package org.homeschoolpebt.app.submission.messages;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoUnit.*;
import static org.homeschoolpebt.app.submission.messages.ScheduledMessages.REMINDER_TIME_FRAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScheduledMessagesTest {
  private static final Instant NOW = Instant.now().atZone(ZoneOffset.UTC)
    .with(ChronoField.HOUR_OF_DAY, 12)
    .with(ChronoField.MINUTE_OF_HOUR, 0)
    .with(ChronoField.SECOND_OF_MINUTE, 0)
    .toInstant();

  @Test
  void testReturnTrueWhenItIsTimeToSendAReminderSubmittedAfterMidnightTwoDaysEarlier() {
    assertTrue(ScheduledMessages.isTimeToSendReminder(
      NOW.with(REMINDER_TIME_FRAME),
      NOW.with(REMINDER_TIME_FRAME).minus(12, HOURS).plusSeconds(1))); // 00:00:01
  }

  @Test
  void testReturnTrueWhenItIsTimeToSendAReminderSubmittedBeforeMidnightTwoDaysEarlier() {
    assertTrue(ScheduledMessages.isTimeToSendReminder(
      NOW.with(REMINDER_TIME_FRAME),
      NOW.with(REMINDER_TIME_FRAME).plus(12, HOURS).minusSeconds(1))); // 23:59:59
  }

  @Test
  void testReturnFalseWhenItIsTooLateToSendAReminderSubmittedBeforeMidnightThreeDaysBefore() {
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      NOW.with(REMINDER_TIME_FRAME),
      NOW.with(REMINDER_TIME_FRAME).minus(12, HOURS).minusSeconds(1))); // 23:59:59
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      NOW.with(REMINDER_TIME_FRAME),
      NOW.with(REMINDER_TIME_FRAME).minus(12, HOURS))); // 00:00:00
  }

  @Test
  void testReturnFalseWhenItIsTooSoonToSendAReminderSubmittedAfterMidnightOneDayBefore() {
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      NOW.with(REMINDER_TIME_FRAME),
      NOW.with(REMINDER_TIME_FRAME).plus(12, HOURS).plusSeconds(1))); // 00:00:01
    assertFalse(ScheduledMessages.isTimeToSendReminder(
      NOW.with(REMINDER_TIME_FRAME),
      NOW.with(REMINDER_TIME_FRAME).plus(12, HOURS))); // 00:00:00
  }
}
