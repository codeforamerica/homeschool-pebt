package org.homeschoolpebt.app.preparers;

import static org.assertj.core.api.Assertions.assertThat;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class IncomePreparerTest {
  @Test
  void includesUnearnedIncome() {
    Submission submission = Submission.builder().inputData(Map.of(
      "incomeUnemploymentAmount", "111",
      "incomeWorkersCompensationAmount", "222",
      "incomeSpousalSupportAmount", "333",
      "incomeChildSupportAmount", "444",
      "incomePensionAmount", "555",
      "incomeRetirementAmount", "666",
      "incomeSSIAmount", "777",
      "incomeOtherAmount", "888"
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission)).isEqualTo(Map.of(
      "income-unemployment", new SingleField("income-unemployment", "111", null),
      "income-workers-comp", new SingleField("income-workers-comp", "222", null),
      "income-spousal-support", new SingleField("income-spousal-support", "333", null),
      "income-child-support", new SingleField("income-child-support", "444", null),
      "income-pension", new SingleField("income-pension", "555", null),
      "income-retirement", new SingleField("income-retirement", "666", null),
      "income-ssi", new SingleField("income-ssi", "777", null),
      "income-other", new SingleField("income-other", "888", null)
    ));
  }
}
