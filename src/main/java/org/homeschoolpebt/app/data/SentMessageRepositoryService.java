package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SentMessageRepositoryService {
  @Autowired
  SentMessageRepository sentMessageRepository;

  public SentMessage save(SentMessage sentMessage) {
    return sentMessageRepository.save(sentMessage);
  }

  public List<Submission> filterPastRecipients(List<Submission> submissions, String messageName) {
    List<SentMessage> sentMessages = sentMessageRepository.findAllBySubmissionsAndMessageName(submissions, messageName);
    Set<UUID> sentSubmissionIds = sentMessages.stream()
        .map(sentMessage -> sentMessage.getSubmission().getId())
          .collect(Collectors.toSet());

    return submissions.stream().filter(submission -> !sentSubmissionIds.contains(submission.getId())).toList();
  }
}
