package org.homeschoolpebt.app.submission.emails;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailGunEmailClient {

  private final String senderEmail;
  private final String mailGunDomain;
  private final String activeProfile;
  private final MailgunMessagesApi mailgunMessagesApi;

  public MailGunEmailClient(@Value("${mail-gun.sender-email}") String senderEmail,
      @Value("${mail-gun.api-key}") String mailGunApiKey,
      @Value("${mail-gun.domain}") String mailGunDomain,
      @Value("${spring.profiles.active:Unknown}") String activeProfile
  ) {
    this.senderEmail = senderEmail;
    this.mailGunDomain = mailGunDomain;
    this.activeProfile = activeProfile;
    this.mailgunMessagesApi = null; /* MailgunClient.config(mailGunApiKey)
        .createApi(MailgunMessagesApi.class); */
  }

  public void sendConfirmationEmail(
      String recipientEmail,
      String applicationId) {
    String subject = "Test confirmation email";
    String emailBody = "<html><body><p>Your confirmation number is: </p><br>%s</body></html>".formatted(applicationId);
    sendEmail(subject, senderEmail, recipientEmail, emailBody);
    log.info("Confirmation email sent for " + applicationId);
  }

  public void sendEmail(String subject, String senderEmail, String recipientEmail, String emailBody) {
    Message message = Message
        .builder()
        .from(senderEmail)
        .to(recipientEmail)
        .subject(subject)
        .html(emailBody)
        .build();

    MessageResponse resp = mailgunMessagesApi.sendMessage(mailGunDomain, message);
  }
}
