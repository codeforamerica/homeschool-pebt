package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import static org.homeschoolpebt.app.utils.SubmissionUtilities.getSubmissionLanguage;

@Component
public class LastMinuteReminderMessage implements PebtMessage {
  Submission submission;

  public LastMinuteReminderMessage(Submission submission) {
    this.submission = submission;
  }

  @Override
  public Email renderEmail() {
    var applicantFullName = SubmissionUtilities.applicantFullName(submission);
    String subject;
    String body;
    if (getSubmissionLanguage(submission).equals("es")) {
      subject = "TODO";
      body = """
        <html>
          <body>
            TODO
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName));
    } else {
      subject = "TODO";
      body = """
        <html>
          <body>
            TODO
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName));
    }
    return new Email(subject, body);
  }

  @Override
  public Sms renderSms() {
    return null;
  }
}
