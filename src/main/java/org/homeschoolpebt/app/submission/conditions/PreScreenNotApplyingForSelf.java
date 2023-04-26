package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class PreScreenNotApplyingForSelf implements Condition {
  @Override
  public Boolean run(Submission submission) {
    if (submission.getInputData().containsKey("isApplyingForSelf")) {
      return submission.getInputData().get("isApplyingForSelf").equals("false");
    }
    return true;
  }
}
