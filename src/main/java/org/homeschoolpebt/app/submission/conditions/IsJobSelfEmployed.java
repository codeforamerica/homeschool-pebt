package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class IsJobSelfEmployed extends AbstractPebtCondition {
  public Boolean run(Submission submission, String uuid) {
    var item = currentIncomeSubflowItem(submission, uuid);

    return item != null && item.getOrDefault("incomeSelfEmployed", "false").equals("true");
  }
}
