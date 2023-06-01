package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JobsPreparerTest {
  @Test
void selfEmploymentStandardDeduction() {
    HashMap<String, Object> job = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeCustomAnnualIncome", "1200");
      put("incomeGrossMonthlyIndividual", "200"); // $200 monthly gross - 40% standard = $120 net
      put("incomeWillBeLessDescription", "I will be planting fewer potatoes.");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$120", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "$200 Gross Monthly Income", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Net Income (40% Deduction)", null),
      "job1-future-monthly-pay", new SingleField("job1-future-monthly-pay", "$100", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "I will be planting fewer potatoes.", null)
    ));
  }

}
