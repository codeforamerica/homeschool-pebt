package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class PreScreenNotEnrolledInVirtualSchool implements Condition {
  @Override
  public Boolean run(Submission submission) {
    if (submission.getInputData().containsKey("isEnrolledInVirtualSchool")) {
      return submission.getInputData().get("isEnrolledInVirtualSchool").equals("false");
    }
    return false;
  }
}
