package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

@Component
public class NeedsUnearnedIncomeDocs implements Condition {

  public Boolean run(Submission submission) {
    var incomeListItems = SubmissionUtilities.getDocUploadUnearnedIncomeList(submission);

    return incomeListItems.size() > 0;
  }
}
