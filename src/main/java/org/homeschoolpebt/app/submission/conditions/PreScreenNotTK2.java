package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class PreScreenNotTK2 implements Condition {
  @Override
  public Boolean run(Submission submission) {
    return submission.getInputData().getOrDefault("isTKto2", "").equals("false");
  }
}
