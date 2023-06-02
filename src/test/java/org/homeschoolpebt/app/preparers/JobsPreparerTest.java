package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.homeschoolpebt.app.inputs.Pebt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JobsPreparerTest {
  @Test
  void selfEmploymentStandardDeduction() {
    var job = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeWillBeLess("true");
      setIncomeSelfEmployed("true");
      setIncomeCustomAnnualIncome("1200");
      setIncomeGrossMonthlyIndividual("200"); // $200 monthly gross - 40% standard = $120 net
      setIncomeWillBeLessDescription("I will be planting fewer potatoes.");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
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
    var job = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeWillBeLess("true");
      setIncomeSelfEmployed("true");
      setIncomeSelfEmployedCustomOperatingExpenses("true");
      setIncomeSelfEmployedOperatingExpenses("100");
      setIncomeCustomAnnualIncome("600");
      setIncomeGrossMonthlyIndividual("200"); // $200 monthly gross - $100 custom operating expenses = $100 net
      setIncomeWillBeLessDescription("My operating expenses are very high.");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
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
    var job = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeSelfEmployed("false");
      setIncomeIsJobHourly("true");
      setIncomeHoursPerWeek("10");
      setIncomeHourlyWage("18"); // Monthly income: $180 (10 * $18)
      setIncomeWillBeLess("false");
      setIncomeWillBeLessDescription("I won't be working as many hours next month.");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$180", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "10.0 hours * $18 per hour", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Gross Income", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "I won't be working as many hours next month.", null)
    ));
  }

  @Test
  void regularPayJob() {
    var job = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeSelfEmployed("false");
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("biweekly"); // Monthly income: $866.67 (400 * 26 / 12)
      setIncomeWillBeLess("false");
      setIncomeWillBeLessDescription("I won't be working as many hours next month.");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job))
    )).build();

    JobsPreparer preparer = new JobsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "job1-employee-name", new SingleField("job1-employee-name", "Johnny Potato", null),
      "job1-name", new SingleField("job1-name", "Tuber", null),
      "job1-past-monthly-pay", new SingleField("job1-past-monthly-pay", "$866.67", null),
      "job1-past-monthly-pay-calculation", new SingleField("job1-past-monthly-pay-calculation", "$400 every 2 weeks", null),
      "job1-pay-type", new SingleField("job1-pay-type", "Gross Income", null),
      "job1-future-pay-comments", new SingleField("job1-future-pay-comments", "I won't be working as many hours next month.", null)
    ));
  }
}
