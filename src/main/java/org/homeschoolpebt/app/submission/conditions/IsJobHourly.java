package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class IsJobHourly extends AbstractPebtCondition {
  public Boolean run(Submission submission, String uuid) {
    var item = currentIncomeSubflowItem(submission, uuid);

    return item != null && item.getOrDefault("incomeIsJobHourly", "false").equals("true");
  }
}
