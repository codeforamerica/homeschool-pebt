package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

@Component
public class HouseholdMemberAlreadyHasIncome implements Condition {

  @Override
  public Boolean run(Submission submission, String data) {
    if (submission.getInputData().containsKey("income")) {
      var memberIterationOptional = SubmissionUtilities
        .jobs(submission)
        .filter(entry -> entry.get("householdMember").equals(data))
        .findFirst();

      if (memberIterationOptional.isPresent()) {
        return true;
      }
    }
    return false;
  }
}
