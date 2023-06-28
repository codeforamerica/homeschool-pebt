package org.homeschoolpebt.app.data;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SentMessageRepositoryService {
  @Autowired
  SentMessageRepository sentMessageRepository;

  public SentMessage save(SentMessage sentMessage) {
    return sentMessageRepository.save(sentMessage);
  }
}
