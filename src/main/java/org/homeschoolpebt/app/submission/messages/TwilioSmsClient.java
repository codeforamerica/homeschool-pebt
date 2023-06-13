package org.homeschoolpebt.app.submission.messages;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwilioSmsClient {
  @Value("${twilio.account.sid}")
  private String twilioAccountSid;
  @Value("${twilio.auth.token}")
  private String twilioAuthToken;
  @Value("${twilio.messaging.service.sid}")
  private String twilioMessagingServiceSid;

  public void sendMessage(String to, String body) {
    Twilio.init(twilioAccountSid, twilioAuthToken);
    Message twilioMessage = Message.creator(new PhoneNumber(to), twilioMessagingServiceSid, body).create();
    log.info("Twilio message request sent; SID=%s; Status=%s".formatted(twilioMessage.getAccountSid(), twilioMessage.getStatus()));
  }
}
