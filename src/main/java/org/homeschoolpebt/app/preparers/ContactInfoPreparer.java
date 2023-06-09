package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ContactInfoPreparer implements SubmissionFieldPreparer {
  @Autowired
  TransmissionRepository transmissionRepository;

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
    SingleField applicantTypeCheckbox;

    if (submission.getInputData().getOrDefault("isApplyingForSelf", "").equals("true")) {
      applicantTypeCheckbox = new SingleField("student", "Yes", null);
    } else if (submission.getInputData().getOrDefault("applicantIsInHousehold", "").equals("true")) {
      applicantTypeCheckbox = new SingleField("household-member", "Yes", null);
    } else if (submission.getInputData().getOrDefault("applicantIsInHousehold", "").equals("false")) {
      applicantTypeCheckbox = new SingleField("assister", "Yes", null);
    } else {
      applicantTypeCheckbox = null;
    }

    var fields = new HashMap<String, SubmissionField>(Map.of(
      "address", new SingleField("address", SubmissionUtilities.combinedAddress(submission), null),
      "zip-code", new SingleField("zip-code", SubmissionUtilities.zipCode(submission), null)
    ));

    if (applicantTypeCheckbox != null) {
      fields.put(applicantTypeCheckbox.getName(), applicantTypeCheckbox);
    }

    var transmission = this.transmissionRepository.getTransmissionBySubmission(submission);
    if (transmission != null) {
      var formattedConfirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
      fields.put("case-number.case-number", new SingleField("case-number.case-number", formattedConfirmationNumber, null));
    }

    return fields;
  }
}
