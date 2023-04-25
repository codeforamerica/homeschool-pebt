package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class PreScreenHasOnlyOneStudent implements Condition {
  @Override
  public Boolean run(Submission submission) {
    if (submission.getInputData().containsKey("hasMoreThanOneStudent") && submission.getInputData().containsKey("isApplyingForSelf")) {
      return submission.getInputData().get("hasMoreThanOneStudent").equals("false") &&
          submission.getInputData().get("isApplyingForSelf").equals("false");
    }
    return false;
  }
}
