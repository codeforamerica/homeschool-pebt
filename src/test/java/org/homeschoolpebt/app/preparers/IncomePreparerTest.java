package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
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
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).containsAllEntriesOf(Map.of(
      "income-unemployment", new SingleField("income-unemployment", "$111", null),
      "income-workers-comp", new SingleField("income-workers-comp", "$222", null),
      "income-spousal-support", new SingleField("income-spousal-support", "$333", null),
      "income-child-support", new SingleField("income-child-support", "$444", null),
      "income-pension", new SingleField("income-pension", "$555", null),
      "income-retirement", new SingleField("income-retirement", "$666", null),
      "income-ssi", new SingleField("income-ssi", "$777", null),
      "income-other", new SingleField("income-other", "$888", null),
      "income-hh-unearned", new SingleField("income-hh-unearned", "$3996", null)
    ));
  }

  @Test
  void testThatTheBigImportantCalculationsAreCorrect() {
    // Self Employment w/Standard Deduction
    HashMap<String, Object> job1 = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeCustomAnnualIncome", "1200"); // Future: $100/mo.
      put("incomeGrossMonthlyIndividual", "200"); // Past: $200 monthly gross - 40% standard = $120 net
      put("incomeWillBeLessDescription", "I will be planting fewer potatoes.");
    }};

    // Self Emploympent w/Custom Deductions
    HashMap<String, Object> job2 = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeSelfEmployedCustomOperatingExpenses", "true");
      put("incomeSelfEmployedOperatingExpenses", "100");
      put("incomeCustomAnnualIncome", "600"); // Future: $50/mo.
      put("incomeGrossMonthlyIndividual", "200"); // $200 monthly gross - $100 custom operating expenses = $100 net
      put("incomeWillBeLessDescription", "My operating expenses are very high.");
    }};

    // Hourly
    HashMap<String, Object> job3 = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeSelfEmployed", "false");
      put("incomeIsJobHourly", "true");
      put("incomeHoursPerWeek", "10");
      put("incomeHourlyWage", "18"); // Monthly income: $180 (10 * $18)
      put("incomeWillBeLess", "false");
      put("incomeWillBeLessDescription", "I won't be working as many hours next month.");
    }};

    // Regular Pay (weekly)
    HashMap<String, Object> job4 = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeSelfEmployed", "false");
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "biweekly"); // Monthly income: $866.67 (400 * 26 / 12)
      put("incomeWillBeLess", "false");
      put("incomeWillBeLessDescription", "I won't be working as many hours next month.");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("income", List.of(job1, job2, job3, job4)),
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
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).containsAllEntriesOf(Map.ofEntries(
      // $3996 = $111 + $222 + ... + $888
      Map.entry("income-hh-unearned", new SingleField("income-hh-unearned", "$3996", null)),

      // $1196.67 = $100 (job1) + $50 (job2) + $180 (job3) + $866.67 (job4)
      Map.entry("income-hh-future-earned", new SingleField("income-hh-future-earned", "$1196.67", null)),

      // $1266.67 = $120 (job1) + $100 (job2) + $180 (job3) + $866.67 (job4)
      Map.entry("income-hh-past-earned", new SingleField("income-hh-past-earned", "$1266.67", null)),

      // $5192.67 = $3996 (unearned) + $1196.67 (income-hh-future-earned)
      Map.entry("income-hh-future-total", new SingleField("income-hh-future-total", "$5192.67", null)),

      // $5262.67 = $3996 (unearned) + $1266.67 (income-hh-past-earned)
      Map.entry("income-hh-past-total", new SingleField("income-hh-past-total", "$5262.67", null))
    ));
  }
}
