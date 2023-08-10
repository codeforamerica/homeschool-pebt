package org.homeschoolpebt.app.cli;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import org.homeschoolpebt.app.data.SentMessage;
import org.homeschoolpebt.app.data.SentMessageRepository;
import org.homeschoolpebt.app.submission.messages.LastMinuteReminderMessage;
import org.homeschoolpebt.app.submission.messages.ScheduledMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class AdHocMessengerTest {
  @Autowired AdHocMessenger adHocMessenger;
  @Autowired SubmissionRepository submissionRepository;
  @Autowired SentMessageRepository sentMessageRepository;
  @MockBean ScheduledMessages scheduledMessages;

  @Test
  void sendsForTheRightSubmission() {
    var unsubmitted1 = Submission.builder().flow("pebt").inputData(new HashMap<>(Map.ofEntries(
      Map.entry("email", "test@example.com")
    ))).build();
    var unsubmitted2 = Submission.builder().flow("pebt").inputData(new HashMap<>(Map.ofEntries(
      Map.entry("email", "test2@example.com")
    ))).build();
    var submitted1 = Submission.builder().flow("pebt").submittedAt(new Date()).inputData(new HashMap<>(Map.ofEntries(
      Map.entry("email", "teSt@example.com")
    ))).build();
    this.submissionRepository.save(unsubmitted1);
    this.submissionRepository.save(unsubmitted2);
    this.submissionRepository.save(submitted1);

    this.adHocMessenger.triggerLastMinuteReminderMessages();

    verify(this.scheduledMessages).sendLastMinuteReminderMessage(unsubmitted2);
    verify(this.scheduledMessages, never()).sendLastMinuteReminderMessage(unsubmitted1);
    verify(this.scheduledMessages, never()).sendLastMinuteReminderMessage(submitted1);
  }

  @Test
  void skipsAnyPastRecipients() {
    var unsubmitted1 = Submission.builder().flow("pebt").inputData(new HashMap<>(Map.ofEntries(
      Map.entry("email", "test@example.com")
    ))).build();
    var sentMessage1 = SentMessage.builder().sentAt(new Date()).messageName(LastMinuteReminderMessage.class.getSimpleName()).submission(unsubmitted1).build();
    this.submissionRepository.save(unsubmitted1);
    this.sentMessageRepository.save(sentMessage1);

    this.adHocMessenger.triggerLastMinuteReminderMessages();

    verify(this.scheduledMessages, never()).sendLastMinuteReminderMessage(unsubmitted1);
  }
}
