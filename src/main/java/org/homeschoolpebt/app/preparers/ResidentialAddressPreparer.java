package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import java.util.Map;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

@Component
public class ResidentialAddressPreparer implements SubmissionFieldPreparer {
  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission) {
    return Map.of(
      "address", new SingleField("address", SubmissionUtilities.combinedAddress(submission), null),
      "zip-code", new SingleField("zip-code", SubmissionUtilities.zipCode(submission), null)
    );
  }
}
