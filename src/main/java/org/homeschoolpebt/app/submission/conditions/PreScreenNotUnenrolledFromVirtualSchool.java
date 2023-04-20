package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class PreScreenNotUnenrolledFromVirtualSchool implements Condition {
  @Override
  public Boolean run(Submission submission) {
    if (submission.getInputData().containsKey("hasUnenrolled")) {
      return submission.getInputData().get("hasUnenrolled").equals("false");
    }
    return false;
  }
}
