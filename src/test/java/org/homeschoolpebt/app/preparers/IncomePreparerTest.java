package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IncomePreparerTest {
  @Test
  void includesUnearnedIncome() {
    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("incomeTypes[]", List.of("incomeUnemployment", "incomeWorkersCompensation", "incomeSpousalSupport", "incomeChildSupport", "incomePension", "incomeRetirement", "incomeSSI", "incomeOther")),
      Map.entry("incomeUnemploymentAmount", "111"),
      Map.entry("incomeWorkersCompensationAmount", "222"),
      Map.entry("incomeSpousalSupportAmount", "333"),
      Map.entry("incomeChildSupportAmount", "444"),
      Map.entry("incomePensionAmount", "555"),
      Map.entry("incomeRetirementAmount", "666"),
      Map.entry("incomeSSIAmount", "777"),
      Map.entry("incomeOtherAmount", "888")
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "income-unemployment", new SingleField("income-unemployment", "111", null),
      "income-workers-comp", new SingleField("income-workers-comp", "222", null),
      "income-spousal-support", new SingleField("income-spousal-support", "333", null),
      "income-child-support", new SingleField("income-child-support", "444", null),
      "income-pension", new SingleField("income-pension", "555", null),
      "income-retirement", new SingleField("income-retirement", "666", null),
      "income-ssi", new SingleField("income-ssi", "777", null),
      "income-other", new SingleField("income-other", "888", null),
      "income-hh-unearned", new SingleField("income-hh-unearned", "3996", null)
    ));
  }
}
