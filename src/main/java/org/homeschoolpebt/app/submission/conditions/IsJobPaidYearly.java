package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class IsJobPaidYearly extends AbstractPebtCondition {
  public Boolean run(Submission submission, String uuid) {
    var item = currentIncomeSubflowItem(submission, uuid);

    return item != null &&
      item.getOrDefault("incomeSelfEmployed", "false").equals("false") &&
      item.getOrDefault("incomeIsJobHourly", "false").equals("false") &&
      (
        item.getOrDefault("incomeRegularPayInterval", "").equals("yearly") ||
        item.getOrDefault("incomeRegularPayInterval", "").equals("seasonally")
      );
  }
}
