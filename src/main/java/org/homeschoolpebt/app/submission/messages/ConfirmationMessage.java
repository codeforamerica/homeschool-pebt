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
    var applicationNumber = SubmissionUtilities.getFormattedApplicationNumber(transmission.getApplicationNumber());
    String subject = "Test confirmation email";

    String emailBody = "<html><body><p>Your confirmation number is: <br>%s</p></body></html>".formatted(
      applicationNumber
    );
    return new Email(subject, emailBody);
  }
}
