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
      Map.entry("incomeUnearnedRetirementTypes[]", List.of("incomeSSI", "incomePension", "incomeSocialSecurity", "income401k403b")),
      Map.entry("incomeSSIAmount", "1"),
      Map.entry("incomePensionAmount", "2"),
      Map.entry("incomeSocialSecurityAmount", "3"),
      Map.entry("income401k403bAmount", "4"),
      Map.entry("incomeUnearnedTypes[]", List.of("incomeUnemployment", "incomeWorkersCompensation", "incomeSpousalSupport", "incomeChildSupport", "incomeDisability", "incomeVeterans", "incomeOther")),
      Map.entry("incomeUnemploymentAmount", "101"),
      Map.entry("incomeWorkersCompensationAmount", "102"),
      Map.entry("incomeSpousalSupportAmount", "103"),
      Map.entry("incomeChildSupportAmount", "104"),
      Map.entry("incomeDisabilityAmount", "105"),
      Map.entry("incomeVeteransAmount", "106"),
      Map.entry("incomeOtherAmount", "200")
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("income-unemployment", new SingleField("income-unemployment", "$101", null)),
      Map.entry("income-workers-comp", new SingleField("income-workers-comp", "$102", null)),
      Map.entry("income-spousal-support", new SingleField("income-spousal-support", "$103", null)),
      Map.entry("income-child-support", new SingleField("income-child-support", "$104", null)),
      Map.entry("income-disability", new SingleField("income-disability", "$105", null)),
      Map.entry("income-veterans", new SingleField("income-veterans", "$106", null)),
      Map.entry("income-other", new SingleField("income-other", "$200", null)),

      Map.entry("income-ssi", new SingleField("income-ssi", "$1", null)),
      Map.entry("income-pension", new SingleField("income-pension", "$2", null)),
      Map.entry("income-social-security", new SingleField("income-social-security", "$3", null)),
      Map.entry("income-401k", new SingleField("income-401k", "$4", null)),

      // $831 = 101+102+103+104+105+106+200+1+2+3+4
      Map.entry("income-hh-unearned", new SingleField("income-hh-unearned", "$831", null)
    )));
  }

  @Test
  void includesUnearnedIncomeWhenMissingFromTypesArray() {
    // This may happen when the client adds some data, and then hits back a few times and unchecks it as a form of unearned income.
    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("incomeUnearnedRetirementTypes[]", List.of("incomeSSI", "incomeSocialSecurity", "income401k403b")),
      Map.entry("incomeSSIAmount", "1"),
      Map.entry("incomePensionAmount", "2"),
      Map.entry("incomeSocialSecurityAmount", "3"),
      Map.entry("income401k403bAmount", "4"),
      Map.entry("incomeUnearnedTypes[]", List.of("incomeUnemployment", "incomeWorkersCompensation", "incomeSpousalSupport", "incomeDisability", "incomeVeterans", "incomeOther")),
      Map.entry("incomeUnemploymentAmount", "101"),
      Map.entry("incomeWorkersCompensationAmount", "102"),
      Map.entry("incomeSpousalSupportAmount", "103"),
      Map.entry("incomeChildSupportAmount", "104"),
      Map.entry("incomeDisabilityAmount", "105"),
      Map.entry("incomeVeteransAmount", "106"),
      Map.entry("incomeOtherAmount", "200")
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("income-unemployment", new SingleField("income-unemployment", "$101", null)),
      Map.entry("income-workers-comp", new SingleField("income-workers-comp", "$102", null)),
      Map.entry("income-spousal-support", new SingleField("income-spousal-support", "$103", null)),
      Map.entry("income-child-support", new SingleField("income-child-support", "$104", null)),
      Map.entry("income-disability", new SingleField("income-disability", "$105", null)),
      Map.entry("income-veterans", new SingleField("income-veterans", "$106", null)),
      Map.entry("income-other", new SingleField("income-other", "$200", null)),

      Map.entry("income-ssi", new SingleField("income-ssi", "$1", null)),
      Map.entry("income-pension", new SingleField("income-pension", "$2", null)),
      Map.entry("income-social-security", new SingleField("income-social-security", "$3", null)),
      Map.entry("income-401k", new SingleField("income-401k", "$4", null)),

      // $831 = 101+102+103+104+105+106+200+1+2+3+4
      Map.entry("income-hh-unearned", new SingleField("income-hh-unearned", "$831", null)
      )));
  }

  @Test
  void includesUnearnedIncomeTreatingNullAsZero() {
    // This may happen in unusual circumstances the client checks a new box for a form of unearned income, then skips the page where they would enter
    // the monthly amount.
    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("incomeUnearnedRetirementTypes[]", List.of("incomeSSI", "incomeSocialSecurity", "income401k403b")),
      Map.entry("incomeSSIAmount", "1"),
      Map.entry("incomePensionAmount", "2"),
      Map.entry("income401k403bAmount", "4"),
      Map.entry("incomeUnearnedTypes[]", List.of("incomeUnemployment", "incomeWorkersCompensation", "incomeSpousalSupport", "incomeChildSupport", "incomeDisability", "incomeVeterans", "incomeOther")),
      Map.entry("incomeUnemploymentAmount", "101"),
      Map.entry("incomeWorkersCompensationAmount", "102"),
      Map.entry("incomeSpousalSupportAmount", "103"),
      Map.entry("incomeChildSupportAmount", "104"),
      Map.entry("incomeDisabilityAmount", "105"),
      Map.entry("incomeVeteransAmount", "106"),
      Map.entry("incomeOtherAmount", "200")
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("income-unemployment", new SingleField("income-unemployment", "$101", null)),
      Map.entry("income-workers-comp", new SingleField("income-workers-comp", "$102", null)),
      Map.entry("income-spousal-support", new SingleField("income-spousal-support", "$103", null)),
      Map.entry("income-child-support", new SingleField("income-child-support", "$104", null)),
      Map.entry("income-disability", new SingleField("income-disability", "$105", null)),
      Map.entry("income-veterans", new SingleField("income-veterans", "$106", null)),
      Map.entry("income-other", new SingleField("income-other", "$200", null)),

      Map.entry("income-ssi", new SingleField("income-ssi", "$1", null)),
      Map.entry("income-pension", new SingleField("income-pension", "$2", null)),
      Map.entry("income-social-security", new SingleField("income-social-security", "", null)),
      Map.entry("income-401k", new SingleField("income-401k", "$4", null)),

      // $828 = 101+102+103+104+105+106+200+1+2+4
      Map.entry("income-hh-unearned", new SingleField("income-hh-unearned", "$828", null)
      )));
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

    // Self Employment w/Custom Deductions
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
      put("incomeHourlyWage", "18"); // Monthly income: $720 (10 * $18 * 4)
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

      Map.entry("incomeUnearnedRetirementTypes[]", List.of("incomeSSI", "incomePension", "incomeSocialSecurity", "income401k403b")),
      Map.entry("incomeSSIAmount", "1"),
      Map.entry("incomePensionAmount", "2"),
      Map.entry("incomeSocialSecurityAmount", "3"),
      Map.entry("income401k403bAmount", "4"),
      Map.entry("incomeUnearnedTypes[]", List.of("incomeUnemployment", "incomeWorkersCompensation", "incomeSpousalSupport", "incomeChildSupport", "incomeDisability", "incomeVeterans", "incomeOther")),
      Map.entry("incomeUnemploymentAmount", "101"),
      Map.entry("incomeWorkersCompensationAmount", "102"),
      Map.entry("incomeSpousalSupportAmount", "103"),
      Map.entry("incomeChildSupportAmount", "104"),
      Map.entry("incomeDisabilityAmount", "105"),
      Map.entry("incomeVeteransAmount", "106"),
      Map.entry("incomeOtherAmount", "200")
    )).build();

    IncomePreparer preparer = new IncomePreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.ofEntries(
      // $831 = 101+102+103+104+105+106+200+1+2+3+4
      Map.entry("income-hh-unearned", new SingleField("income-hh-unearned", "$831", null)),

      // $1736.67 = $100 (job1) + $50 (job2) + $720 (job3) + $866.67 (job4)
      Map.entry("income-hh-future-earned", new SingleField("income-hh-future-earned", "$1736.67", null)),

      // $1806.67 = $120 (job1) + $100 (job2) + $720 (job3) + $866.67 (job4)
      Map.entry("income-hh-past-earned", new SingleField("income-hh-past-earned", "$1806.67", null)),

      // $2567.67 = sum([831, 1736.67]) # (unearned) + (income-hh-future-earned)
      Map.entry("income-hh-future-total", new SingleField("income-hh-future-total", "$2567.67", null)),

      // $2637.67 = sum([831, 1806.67]) # (unearned) + (income-hh-past-earned)
      Map.entry("income-hh-past-total", new SingleField("income-hh-past-total", "$2637.67", null))
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
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.of(
      "household-count", new SingleField("household-count", "3", null)
    ));
  }
}
