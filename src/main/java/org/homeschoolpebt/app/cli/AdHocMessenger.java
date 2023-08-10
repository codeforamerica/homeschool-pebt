package org.homeschoolpebt.app.cli;

import formflow.library.data.Submission;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.data.SentMessageRepositoryService;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.submission.messages.LastMinuteReminderMessage;
import org.homeschoolpebt.app.submission.messages.ScheduledMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ShellComponent
public class AdHocMessenger {
  @Autowired
  TransmissionRepository transmissionRepository;
  @Autowired
  ScheduledMessages scheduledMessages;
  @Autowired SentMessageRepositoryService sentMessageRepositoryService;

  @ShellMethod(key = "triggerLastMinuteReminderMessages")
  void triggerLastMinuteReminderMessages() {
    log.info("Getting list of submitted and unsubmitted emails");
    List<Submission> submitted = this.transmissionRepository.submissionsSubmittedWithEmail();
    List<Submission> unsubmitted = this.transmissionRepository.submissionsUnsubmittedWithEmail();

    Set<String> submittedEmails = submitted.stream().map(submission -> normalizeEmail((String) submission.getInputData().get("email"))).collect(Collectors.toSet());
    List<Submission> submissionsThatNeedReminders = unsubmitted.stream().filter(submission -> {
      var email = normalizeEmail((String) submission.getInputData().get("email"));
      return !submittedEmails.contains(email);
    }).toList();
    List<Submission> submissionsToSendReminders = sentMessageRepositoryService.filterPastRecipients(submissionsThatNeedReminders, LastMinuteReminderMessage.class.getSimpleName());
    log.info("Found {} submissions that need the LastMinuteReminderMessage", submissionsToSendReminders.size());

    for (var submission : submissionsToSendReminders) {
      scheduledMessages.sendLastMinuteReminderMessage(submission);
    }
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase();
  }
}
