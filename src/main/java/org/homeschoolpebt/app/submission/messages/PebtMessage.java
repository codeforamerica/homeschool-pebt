package org.homeschoolpebt.app.submission.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public interface PebtMessage {
  Email renderEmail();

  @Getter
  @Setter
  @AllArgsConstructor
  class Email {
    public String subject;
    public String bodyHtml;
  }
}
