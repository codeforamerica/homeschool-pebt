package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.inputs.Pebt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeCalculatorTest {
  @Test
  void unearnedIncome() {
    Submission submission = Submission.builder().inputData(Map.of(
      "incomeTypes[]", List.of(Pebt.INCOME_TYPES.incomeUnemployment, Pebt.INCOME_TYPES.incomeSSI),
      "incomeUnemploymentAmount", "111",
      "incomeSSIAmount", "222",
      "incomeChildSupportAmount", "123" // <- should be ignored since child support isn't in incomeTypes
    )).build();

    IncomeCalculator calc = new IncomeCalculator(submission);
    assertThat(calc.totalUnearnedIncome()).isEqualTo(333.0);
  }
}
