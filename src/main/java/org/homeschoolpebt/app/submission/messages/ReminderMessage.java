package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

@Component
public class ReminderMessage implements PebtMessage {
  Submission submission;
  Transmission transmission;

  public ReminderMessage(Submission submission, Transmission transmission) {
    this.submission = submission;
    this.transmission = transmission;
  }

  @Override
  public Email renderEmail() {
    var confirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
    var applicantFullName = SubmissionUtilities.applicantFullName(submission);
    String subject = "Documents Needed for P-EBT 4.0 Application";
    String body = """
      <html>
        <body>
          <p>Dear %s,</p>
          <p>Thank you for beginning the application for P-EBT benefits. This is a reminder to upload the documents for your application.\s
          You will need proof of income, a student  ID and virtual school documentation for each student, if applicable.</p>
          <p>You can find the link here: https://getpebt.org/%s</p>
          <p>Your application number is %s.</p>
          <p>If you need food now, you can contact your local food bank at https://www.cafoodbanks.org/find-food. You can also apply for CalFresh at GetCalFresh.org.</p>
          <p>- California Department of Social Services</p>
        </body>
      </html>
      """.formatted(StringUtils.escapeXml(applicantFullName), "DOCSLINK", confirmationNumber);
    return new Email(subject, body);
  }

  @Override
  public Sms renderSms() {
    var confirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
    String body = """
      Thank you for beginning the application for P-EBT benefits. This is a reminder to upload the documents for your application.\s
      You will need to upload proof of identity for each student, proof of income, and virtual school documentation for each student, if applicable.\s
      Your application number is %s and you can find the link here: https://getpebt.org/%s
      """.formatted(confirmationNumber, "DOCSLINK");
    return new Sms(body);
  }
}
