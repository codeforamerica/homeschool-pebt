package org.homeschoolpebt.app.utils;

import static org.assertj.core.api.Assertions.assertThat;

import formflow.library.data.Submission;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IncomeCalculatorTest {
  @Test
  void unearnedIncome() {
    Submission submission = Submission.builder().inputData(Map.of(
      "incomeTypes[]", List.of("incomeUnemployment", "incomeSSI"),
      "incomeUnemploymentAmount", "111",
      "incomeSSIAmount", "222",
      "incomeChildSupportAmount", "123" // <- should be ignored since child support isn't in incomeTypes
    )).build();

    IncomeCalculator calc = new IncomeCalculator(submission);
    assertThat(calc.totalUnearnedIncome()).isEqualTo(new BigDecimal(333));
  }
}