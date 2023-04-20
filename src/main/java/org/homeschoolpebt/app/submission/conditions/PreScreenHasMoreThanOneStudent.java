package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class PreScreenHasMoreThanOneStudent implements Condition {
  @Override
  public Boolean run(Submission submission) {
    if (submission.getInputData().containsKey("hasMoreThanOneStudent")) {
      return submission.getInputData().get("hasMoreThanOneStudent").equals("true");
    }
    return false;
  }
}
