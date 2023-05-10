package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class HasBlankEmail implements Condition {

  public Boolean run(Submission submission) {
    return (submission.getInputData().get("email") == null ||
            submission.getInputData().get("email").toString().isBlank());
  }
}
