package org.homeschoolpebt.app.preparers;

import static org.assertj.core.api.Assertions.assertThat;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class IncomePreparerTest {

  @Test
  void returnsCalculatedIncome() {
    var subflowItems = new ArrayList<Map<String, Object>>();
    subflowItems.add(Map.of("incomeGrossMonthlyIndividual", "123"));

    Submission submission = Submission.builder().inputData(Map.of("income", subflowItems)).build();
    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission)).isEqualTo(
        Map.of("totalIncome", new SingleField("totalIncome", "123", null))
    );
  }
}
