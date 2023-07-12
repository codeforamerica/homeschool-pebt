package org.homeschoolpebt.app.submission.actions;

import formflow.library.data.FormSubmission;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ClearIncomePeriodicPayFieldsAfterJobHourlyTest {
  @Test
  void clearsIntervalFieldsWhenHourly() {
    var formsubmission = new FormSubmission(new HashMap(Map.of(
      "incomeIsJobHourly", "true"
    )));
    new ClearIncomePeriodicPayFieldsAfterJobHourly().run(formsubmission, null, null);
    assertThat(formsubmission.getFormData().get("incomeRegularPayInterval")).isEqualTo("");
    assertThat(formsubmission.getFormData().get("incomeRegularPayAmount")).isEqualTo("");
    assertThat(formsubmission.getFormData().get("incomeGrossMonthlyIndividual")).isEqualTo("");
  }

  @Test
  void retainsIntervalFieldsWhenNotHourly() {
    var formsubmission = new FormSubmission(new HashMap(Map.of(
      "incomeIsJobHourly", "false"
    )));
    new ClearIncomeFieldsAfterRegularPayCalculator().run(formsubmission, null, null);
    assertThat(formsubmission.getFormData().containsKey("incomeRegularPayInterval")).isFalse();
    assertThat(formsubmission.getFormData().containsKey("incomeRegularPayAmount")).isFalse();
    assertThat(formsubmission.getFormData().containsKey("incomeGrossMonthlyIndividual")).isFalse();
  }
}
