package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class IsJobSelfEmployed extends AbstractPebtCondition {
  public Boolean run(Submission submission, String uuid) {
    var item = currentSubflowItem(submission, uuid);

    return item != null && item.getOrDefault("incomeWasSelfEmployed", "false").equals("true");
  }
}
