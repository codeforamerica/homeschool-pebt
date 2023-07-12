package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import formflow.library.data.Submission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClearIncomeFieldsAfterRegularPayCalculator implements Action {
  @Override
  public void run(FormSubmission formsubmission, Submission submission, String id) {
    String selection = (String) formsubmission.getFormData().get("incomeRegularPayInterval");
    if (selection != null && !selection.isBlank()) {
      formsubmission.getFormData().put("incomeHourlyWage", "");
      formsubmission.getFormData().put("incomeHoursPerWeek", "");
      formsubmission.getFormData().put("incomeGrossMonthlyIndividual", "");
    }
  }
}
