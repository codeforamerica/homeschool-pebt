package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class IsJobHourly extends AbstractPebtCondition {
  public Boolean run(Submission submission, String uuid) {
    var item = currentSubflowItem(submission, uuid);

    return item != null && item.getOrDefault("incomeIsJobHourly", "false").equals("true");
  }
}
