package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import formflow.library.data.Submission;
import lombok.extern.slf4j.Slf4j;
import org.homeschoolpebt.app.submission.emails.MailGunEmailClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendConfirmationEmail implements Action {
  private final MailGunEmailClient mailgun;

  public SendConfirmationEmail(MailGunEmailClient mailgun) {
    this.mailgun = mailgun;
  }

  public void run(Submission submission) {
    mailgun.sendConfirmationEmail(submission.getInputData().get("email").toString(), "123456789");
  }
}
