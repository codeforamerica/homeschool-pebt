package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

@Component
public class IsRequiredToVerifyIncome extends AbstractPebtCondition {
  public Boolean run(Submission submission) {
    return SubmissionUtilities.isRequiredToVerifyIncome(submission);
  }
}
