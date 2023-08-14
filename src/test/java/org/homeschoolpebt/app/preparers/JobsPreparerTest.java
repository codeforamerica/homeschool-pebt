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
      put("iterationIsComplete", true);
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$120", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "$200 Gross Income", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Net Income (40% Deduction)", null),
      "job1-future-monthly-pay", new SingleField("job1-future-monthly-pay", "$100", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "I will be planting fewer potatoes.", null)
    ));
  }

  @Test
  void selfEmploymentUseCustomExpenses() {
    HashMap<String, Object> job = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeSelfEmployedCustomOperatingExpenses", "true");
      put("incomeSelfEmployedOperatingExpenses", "100");
      put("incomeCustomAnnualIncome", "600");
      put("incomeGrossMonthlyIndividual", "200"); // $200 monthly gross - $100 custom operating expenses = $100 net
      put("incomeWillBeLessDescription", "My operating expenses are very high.");
      put("iterationIsComplete", true);
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$100", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "$200 Gross Income - $100 Expenses", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Net Income (After Expenses)", null),
      "job1-future-monthly-pay", new SingleField("job1-future-monthly-pay", "$50", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "My operating expenses are very high.", null)
    ));
  }

  @Test
  void hourlyJob() {
    HashMap<String, Object> job = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeSelfEmployed", "false");
      put("incomeIsJobHourly", "true");
      put("incomeHoursPerWeek", "10");
      put("incomeHourlyWage", "18"); // Monthly income: $720 (10 * $18 * 4)
      put("incomeWillBeLess", "false");
      put("incomeWillBeLessDescription", "I won't be working as many hours next month.");
      put("iterationIsComplete", true);
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$720", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "10.0 hrs/wk * $18/hr * 4 weeks", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Gross Income", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "I won't be working as many hours next month.", null)
    ));
  }

  @Test
  void regularPayJob() {
    HashMap<String, Object> job = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeSelfEmployed", "false");
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "biweekly"); // Monthly income: $866.67 (400 * 26 / 12)
      put("incomeWillBeLess", "false");
      put("incomeWillBeLessDescription", "I won't be working as many hours next month.");
      put("iterationIsComplete", true);
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$866.67", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "$400 every 2 weeks", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Gross Income", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "I won't be working as many hours next month.", null)
    ));
  }
}
