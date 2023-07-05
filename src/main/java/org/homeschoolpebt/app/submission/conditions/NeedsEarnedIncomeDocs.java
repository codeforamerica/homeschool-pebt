package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

@Component
public class NeedsEarnedIncomeDocs implements Condition {
  @Override
  public Boolean run(Submission submission) {
    return SubmissionUtilities.needsEarnedIncomeDocuments(submission);
  }
}
