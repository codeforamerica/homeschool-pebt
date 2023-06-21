package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmationMessage implements PebtMessage {
  Submission submission;
  Transmission transmission;

  @Autowired
  TransmissionRepositoryService transmissionRepositoryService;

  public ConfirmationMessage(Submission submission, Transmission transmission) {
    this.submission = submission;
    this.transmission = transmission;
  }

  @Override
  public Email renderEmail() {
    var confirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
    var applicantFullName = SubmissionUtilities.applicantFullName(submission);
    String subject = "Application Submitted for P-EBT 4.0";
    String body = """
      <html>
        <body>
          <p>Dear %s</p>
          <p>Thank you for submitting your application for P-EBT benefits for the 2022-2023 school year.\s
          You should hear back from the California Department of Social Services by phone or mail in the next 2-4 weeks.</p>
          <p>Your application number is %s.</p>
          <p>If you need food now, you can contact your local food bank at https://www.cafoodbanks.org/find-food. You can also apply for CalFresh at GetCalFresh.org.</p>
          <p>- California Department of Social Services</p>
        </body>
      </html>
      """.formatted(confirmationNumber, applicantFullName);
    return new Email(subject, body);
  }

  @Override
  public Sms renderSms() {
    var confirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
    String body = """
      Thank you for submitting the application for P-EBT benefits for the 2022-2023 school year. You will hear back within 2-4 weeks. Your application number is %s.\s
      -CDSS (California Dept of Social Services)
      """.formatted(confirmationNumber);
    return new Sms(body);
  }
}
