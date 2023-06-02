package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.homeschoolpebt.app.inputs.Pebt;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IncomePreparerTest {
  @Test
  void includesUnearnedIncome() {
    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("incomeTypes[]", List.of(Pebt.INCOME_TYPES.incomeUnemployment, Pebt.INCOME_TYPES.incomeWorkersCompensation, Pebt.INCOME_TYPES.incomeSpousalSupport, Pebt.INCOME_TYPES.incomeChildSupport, Pebt.INCOME_TYPES.incomePension, Pebt.INCOME_TYPES.incomeRetirement, Pebt.INCOME_TYPES.incomeSSI, Pebt.INCOME_TYPES.incomeOther)),
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
    var job1 = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeWillBeLess("true");
      setIncomeSelfEmployed("true");
      setIncomeCustomAnnualIncome("1200"); // Future: $100/mo.
      setIncomeGrossMonthlyIndividual("200"); // Past: $200 monthly gross - 40% standard = $120 net
      setIncomeWillBeLessDescription("I will be planting fewer potatoes.");
    }};

    // Self Emploympent w/Custom Deductions
    var job2 = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeWillBeLess("true");
      setIncomeSelfEmployed("true");
      setIncomeSelfEmployedCustomOperatingExpenses("true");
      setIncomeSelfEmployedOperatingExpenses("100");
      setIncomeCustomAnnualIncome("600"); // Future: $50/mo.
      setIncomeGrossMonthlyIndividual("200"); // $200 monthly gross - $100 custom operating expenses = $100 net
      setIncomeWillBeLessDescription("My operating expenses are very high.");
    }};

    // Hourly
    var job3 = new Pebt.Income() {{
      setIncomeMember("Johnny Potato");
      setIncomeJobName("Tuber");
      setIncomeSelfEmployed("false");
      setIncomeIsJobHourly("true");
      setIncomeHoursPerWeek("10");
      setIncomeHourlyWage("18"); // Monthly income: $180 (10 * $18)
      setIncomeWillBeLess("false");
      setIncomeWillBeLessDescription("I won't be working as many hours next month.");
    }};

    // Regular Pay (weekly)
    var job4 = new Pebt.Income() {{
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
      Map.entry("income", List.of(job1, job2, job3, job4)),
      Map.entry("incomeTypes[]", List.of(Pebt.INCOME_TYPES.incomeUnemployment, Pebt.INCOME_TYPES.incomeWorkersCompensation, Pebt.INCOME_TYPES.incomeSpousalSupport, Pebt.INCOME_TYPES.incomeChildSupport, Pebt.INCOME_TYPES.incomePension, Pebt.INCOME_TYPES.incomeRetirement, Pebt.INCOME_TYPES.incomeSSI, Pebt.INCOME_TYPES.incomeOther)),
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

  @Test
  void includesHouseholdCount() {
    HashMap<String, Object> householdMember = new HashMap<>() {{
      put("householdMemberFirstName", "Bob");
      put("householdMemberLastName", "Barker");
    }};
    HashMap<String, Object> student = new HashMap<>() {{
      put("studentFirstName", "Drew");
      put("studentLastName", "Carey");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("household", List.of(householdMember)),
      Map.entry("students", List.of(student))
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).containsAllEntriesOf(Map.of(
      "household-count", new SingleField("household-count", "3", null)
    ));
  }
}
