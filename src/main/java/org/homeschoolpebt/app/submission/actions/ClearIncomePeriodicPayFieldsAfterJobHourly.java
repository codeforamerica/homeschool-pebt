package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import formflow.library.data.Submission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ClearIncomePeriodicPayFieldsAfterJobHourly implements Action {
  @Override
  public void run(FormSubmission formsubmission, Submission submission, String id) {
    if ("true".equals(formsubmission.getFormData().get("incomeIsJobHourly"))) {
      formsubmission.getFormData().putAll(Map.of(
        "incomeRegularPayInterval", "",
        "incomeRegularPayAmount", "",
        "incomeGrossMonthlyIndividual", ""
      ));
    }
  }
}
