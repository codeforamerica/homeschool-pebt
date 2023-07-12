package org.homeschoolpebt.app.submission.actions;

import formflow.library.data.FormSubmission;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ClearIncomeFieldsAfterRegularPayCalculatorTest {
  @Test
  void clearsHourlyFieldsWhenIntervalPresent() {
    var formsubmission = new FormSubmission(new HashMap(Map.of(
      "incomeRegularPayInterval", "monthly"
    )));
    new ClearIncomeFieldsAfterRegularPayCalculator().run(formsubmission, null, null);
    assertThat(formsubmission.getFormData().get("incomeHourlyWage")).isEqualTo("");
    assertThat(formsubmission.getFormData().get("incomeGrossMonthlyIndividual")).isEqualTo("");
    assertThat(formsubmission.getFormData().get("incomeHoursPerWeek")).isEqualTo("");
  }

  @Test
  void doesNotChangeHourlyFieldsWhenIntervalAbsent() {
    var formsubmission = new FormSubmission(new HashMap(Map.of(
      "incomeRegularPayInterval", ""
    )));
    new ClearIncomeFieldsAfterRegularPayCalculator().run(formsubmission, null, null);
    assertThat(formsubmission.getFormData().containsKey("incomeHourlyWage")).isFalse();
    assertThat(formsubmission.getFormData().containsKey("incomeGrossMonthlyIndividual")).isFalse();
    assertThat(formsubmission.getFormData().containsKey("incomeHoursPerWeek")).isFalse();
  }
}
