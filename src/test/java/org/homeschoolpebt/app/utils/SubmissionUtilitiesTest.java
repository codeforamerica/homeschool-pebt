package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubmissionUtilitiesTest {

  @Test
  void formatMoneyAddsDollarSign() {
    assertEquals("$1", SubmissionUtilities.formatMoney("1"));
  }

  @Test
  @Disabled
  void formatMoneyAddsDecimalPlacesWhenNeeded() {

  }

  @Test
  @Disabled
  void formatMoneyDoesntAddDecimalPlacesForWholeDollarAmounts() {
  }

  @Test
  void formatMoneyAllowsNonNumericStringsToPassThrough() {
    assertEquals("don't know", SubmissionUtilities.formatMoney("don't know"));
  }

  @Test
  void formatMoneyReturnsEmptyStringWhenGivenNull() {
    assertEquals("", SubmissionUtilities.formatMoney((String) null));
  }

  @Test
  @Disabled("Before re-enabling comma handling, ensure commas show up properly in incomeSelfEmployedWillBeLess.html")
  void formatMoneyAddsCommaForThousands() {
    assertEquals("$1,000", SubmissionUtilities.formatMoney("1000"));
  }

  @Test
  void hourlyGrossIncomeAmountIsCorrect() {
    Map<String, Object> job = new HashMap<>() {{
      put("incomeIsJobHourly", "true");
      put("incomeHoursPerWeek", "10");
      put("incomeHourlyWage", "18");
    }};

    assertEquals(SubmissionUtilities.getHourlyGrossIncomeAmount(job), 720.0);
  }

  @Test
  void hourlyGrossIncomeDescriptionIsCorrect() {
    Map<String, Object> job = new HashMap<>() {{
      put("incomeIsJobHourly", "true");
      put("incomeHoursPerWeek", "10");
      put("incomeHourlyWage", "18");
    }};

    assertEquals(SubmissionUtilities.getHourlyGrossIncomeExplanation(job), "10.0 hrs/wk * $18/hr * 4 weeks");
  }

  @Test
  void regularPayAmountIsCorrect() {
    // $400 * weekly = $400 * 52 / 12 = $1733.33 monthly
    Map<String, Object> jobWeekly = new HashMap<>() {{
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "weekly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobWeekly), 1733.3333333333333333333);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobWeekly), "$400 every week");

    // $400 * every 2 weeks = $400 * 26 / 12 = $866.67 monthly
    Map<String, Object> jobBiweekly = new HashMap<>() {{
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "biweekly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobBiweekly), 866.6666666666666666);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobBiweekly), "$400 every 2 weeks");

    // $400 * twice a month = $400 * 24 / 12 = $800
    Map<String, Object> jobSemimonthly = new HashMap<>() {{
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "semimonthly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobSemimonthly), 800.0);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobSemimonthly), "$400 twice a month");

    // $400 * monthly = $400
    Map<String, Object> jobMonthly = new HashMap<>() {{
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "monthly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobMonthly), 400.0);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobMonthly), "$400 monthly");

    // $400 * seasonally = $400 / 12 = $33.33
    Map<String, Object> jobSeasonally = new HashMap<>() {{
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "seasonally");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobSeasonally), 33.333333333333333333);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobSeasonally), "$400 seasonally");

    // $400 * yearly = $400 / 12 = $33.33
    Map<String, Object> jobYearly = new HashMap<>() {{
      put("incomeIsJobHourly", "false");
      put("incomeRegularPayAmount", "400");
      put("incomeRegularPayInterval", "yearly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobYearly), 33.3333333333333333);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobYearly), "$400 yearly");
  }

  @Nested
  class HouseholdReviewTests {
    @Test
    void selfEmployedPastCustomExpenses() {
      Map<String, Object> job = new HashMap<>() {{
        put("uuid", "123-456-789");
        put("incomeMember", "George Washington Carver");
        put("incomeJobName", "Apple");
        put("incomeSelfEmployed", "true");
        put("incomeSelfEmployedCustomOperatingExpenses", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $500 (= $1000 monthly - $500 expenses)
        put("incomeSelfEmployedOperatingExpenses", "500");
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("income", List.of(job))
      )).build();

      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var householdReviewItem = items.get(0);
      assertThat(householdReviewItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "George Washington Carver"),
        Map.entry("income", "$500"),
        Map.entry("jobName", "Apple"),
        Map.entry("incomeType", "net-pay"),
        Map.entry("uuid", "123-456-789")
      ));
    }

    @Test
    void selfEmployedPastStandardDeduction() {
      Map<String, Object> job = new HashMap<>() {{
        put("uuid", "123-456-789");
        put("incomeMember", "George Washington Carver");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("income", List.of(job))
      )).build();

      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var householdReviewItem = items.get(0);
      assertThat(householdReviewItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "George Washington Carver"),
        Map.entry("income", "$600"),
        Map.entry("incomeType", "net-pay"),
        Map.entry("uuid", "123-456-789")
      ));
    }

    @Test
    void withDifferentFutureIncome() {
      Map<String, Object> job = new HashMap<>() {{
        put("uuid", "123-456-789");
        put("incomeMember", "George Washington Carver");
        put("incomeSelfEmployed", "true");
        put("incomeWillBeLess", "true");
        put("incomeCustomAnnualIncome", "1200"); // Gross income: $100 (= $1200 / 12)
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("income", List.of(job))
      )).build();

      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var householdReviewItem = items.get(0);
      assertThat(householdReviewItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "George Washington Carver"),
        Map.entry("income", "$100"),
        Map.entry("incomeType", "net-pay-estimate"),
        Map.entry("uuid", "123-456-789")
      ));
    }

    @Test
    void withDifferentFutureIncomeRegularIntervtal() {
      Map<String, Object> job = new HashMap<>() {{
        put("uuid", "123-456-789");
        put("incomeMember", "George Washington Carver");
        put("incomeSelfEmployed", "false");
        put("incomeIsJobHourly", "false");
        put("incomeRegularPayInterval", "semimonthly");
        put("incomeRegularPayAmount", "200");
        put("incomeWillBeLess", "true");
        put("incomeCustomMonthlyIncome", "150");
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("income", List.of(job))
      )).build();

      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var householdReviewItem = items.get(0);
      assertThat(householdReviewItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "George Washington Carver"),
        Map.entry("income", "$150"),
        Map.entry("incomeType", "gross-pay-estimate"),
        Map.entry("uuid", "123-456-789")
      ));
    }

    @Test
    void sortsApplicantFirst() {
      Map<String, Object> job1 = new HashMap<>() {{
        put("uuid", "123-456-789");
        put("incomeMember", "George Washington Carver");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      Map<String, Object> job2 = new HashMap<>() {{
        put("uuid", "abc-def-ghi");
        put("incomeMember", "Johnny Appleseed");
        put("incomeIsJobHourly", "true");
        put("incomeHourlyWage", "10");
        put("incomeHoursPerWeek", "18");
        put("incomeWillBeLess", "true");
        put("incomeCustomMonthlyIncome", "100"); // Est. Gross income: $100
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("income", List.of(job1, job2)),
        Map.entry("firstName", "Johnny"),
        Map.entry("lastName", "Appleseed")
      )).build();
      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);

      var firstItem = items.get(0);
      assertThat(firstItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Johnny Appleseed"),
        Map.entry("isApplicant", true)
      ));
      var secondItem = items.get(1);
      assertThat(secondItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "George Washington Carver"),
        Map.entry("isApplicant", false)
      ));
    }

    @Test
    void includesMembersWithoutJobs() {
      var student1 = new HashMap<String, Object>() {{
        put("studentFirstName", "Sally");
        put("studentMiddleInitial", "A");
        put("studentLastName", "Starfish");
      }};

      var householdMember1 = new HashMap<String, Object>() {{
        put("householdMemberFirstName", "Teddy");
        put("householdMemberLastName", "Trout");
      }};

      var householdMember2 = new HashMap<String, Object>() {{
        put("householdMemberFirstName", "Ursula");
        put("householdMemberLastName", "Unicorn");
      }};

      Map<String, Object> job1 = new HashMap<>() {{
        put("uuid", "123-456-789");
        put("incomeMember", "Ursula Unicorn");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("firstName", "Johnny"),
        Map.entry("lastName", "Appleseed"),
        Map.entry("household", List.of(householdMember1, householdMember2)),
        Map.entry("students", List.of(student1)),
        Map.entry("income", List.of(job1))
      )).build();
      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var applicantItem = items.get(0);
      assertThat(applicantItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Johnny Appleseed"),
        Map.entry("isApplicant", true),
        Map.entry("itemType", "no-jobs-added")
      ));

      var firstJobItem = items.get(1);
      assertThat(firstJobItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Sally A Starfish"),
        Map.entry("itemType", "no-jobs-added")
      ));

      var nonJobFirstItem = items.get(2);
      assertThat(nonJobFirstItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Teddy Trout"),
        Map.entry("itemType", "no-jobs-added")
      ));

      var nonJobSecondItem = items.get(3);
      assertThat(nonJobSecondItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Ursula Unicorn"),
        Map.entry("isApplicant", false),
        Map.entry("itemType", "job")
      ));
    }

    @Test
    void combinesSequentialItems() {
      var student1 = new HashMap<String, Object>() {{
        put("studentFirstName", "Sally");
        put("studentMiddleInitial", "A");
        put("studentLastName", "Starfish");
      }};

      Map<String, Object> job1 = new HashMap<>() {{
        put("uuid", "111-111-111");
        put("incomeMember", "Sally A Starfish");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      Map<String, Object> job2 = new HashMap<>() {{
        put("uuid", "222-222-222");
        put("incomeMember", "Johnny Appleseed");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      Map<String, Object> job3 = new HashMap<>() {{
        put("uuid", "333-333-333");
        put("incomeMember", "Sally A Starfish");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      Map<String, Object> job4 = new HashMap<>() {{
        put("uuid", "444-444-444");
        put("incomeMember", "Johnny Appleseed");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 expenses)
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("firstName", "Johnny"),
        Map.entry("lastName", "Appleseed"),
        Map.entry("students", List.of(student1)),
        Map.entry("income", List.of(job1, job2, job3, job4))
      )).build();
      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var applicantItem1 = items.get(0);
      assertThat(applicantItem1).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Johnny Appleseed"),
        Map.entry("isApplicant", true),
        Map.entry("combineWithPrevious", false),
        Map.entry("itemType", "job")
      ));

      var applicantItem2 = items.get(1);
      assertThat(applicantItem2).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Johnny Appleseed"),
        Map.entry("isApplicant", true),
        Map.entry("combineWithPrevious", true),
        Map.entry("itemType", "job")
      ));

      var nonApplicantItem1 = items.get(2);
      assertThat(nonApplicantItem1).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Sally A Starfish"),
        Map.entry("isApplicant", false),
        Map.entry("combineWithPrevious", false),
        Map.entry("itemType", "job")
      ));

      var nonApplicantItem2 = items.get(3);
      assertThat(nonApplicantItem2).containsAllEntriesOf(Map.ofEntries(
        Map.entry("name", "Sally A Starfish"),
        Map.entry("isApplicant", false),
        Map.entry("combineWithPrevious", true),
        Map.entry("itemType", "job")
      ));
    }

    @Test
    void addsTotalFutureHouseholdTotal() {
      var student1 = new HashMap<String, Object>() {{
        put("studentFirstName", "Sally");
        put("studentMiddleInitial", "A");
        put("studentLastName", "Starfish");
      }};

      var student2 = new HashMap<String, Object>() {{
        put("studentFirstName", "George");
        put("studentLastName", "Washington Carver");
      }};

      Map<String, Object> job1 = new HashMap<>() {{
        put("uuid", "111");
        put("incomeMember", "George Washington Carver");
        put("incomeJobName", "Apple");
        put("incomeSelfEmployed", "true");
        put("incomeSelfEmployedCustomOperatingExpenses", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $500 (= $1000 monthly - $500 expenses)
        put("incomeSelfEmployedOperatingExpenses", "500");
      }};

      Map<String, Object> job2 = new HashMap<>() {{
        put("uuid", "222");
        put("incomeMember", "Sally A Starfish");
        put("incomeSelfEmployed", "true");
        put("incomeGrossMonthlyIndividual", "1000"); // Net income: $600 (= $1000 monthly - $400 standard deduction)
      }};

      Map<String, Object> job3 = new HashMap<>() {{
        put("uuid", "333");
        put("incomeMember", "Johnny Appleseed");
        put("incomeIsJobHourly", "true");
        put("incomeHourlyWage", "10");
        put("incomeHoursPerWeek", "18"); // Gross Income: $720 (= 10 hr/wk * $18 / hr * 4 wk/mo)
      }};

      Map<String, Object> job4 = new HashMap<>() {{
        put("uuid", "444");
        put("incomeMember", "Johnny Appleseed");
        put("incomeIsJobHourly", "false");
        put("incomeRegularPayInterval", "weekly");
        put("incomeRegularPayAmount", "100"); // Gross Income: $433.33 (= $100 * 4.33333 weeks/mo)
      }};

      var submission = Submission.builder().inputData(Map.ofEntries(
        Map.entry("firstName", "Johnny"),
        Map.entry("lastName", "Appleseed"),
        Map.entry("students", List.of(student1, student2)),
        Map.entry("income", List.of(job1, job2, job3, job4))
      )).build();

      var items = SubmissionUtilities.getHouseholdIncomeReviewItems(submission);
      var totalItem = items.get(items.size() - 1);
      assertThat(totalItem).containsAllEntriesOf(Map.ofEntries(
        Map.entry("itemType", "household-total"),
        // $2253.33 = $500 (job1) + $600 (job2) + $720 (job3) + $433.33 (job4)
        Map.entry("income", "$2253.33")
      ));
    }
  }

  @Test
  void formatApplicationNumber() {
    String test1 = "001000144";
    assertThat(SubmissionUtilities.getFormattedApplicationNumber(test1)).isEqualTo("1000144");
  }

  @Test
  void docUploadIdentityStudentsListContainsStudents() {
    var student1 = new HashMap<String, Object>() {{
      put("studentFirstName", "Sally");
      put("studentMiddleInitial", "A");
      put("studentLastName", "Starfish");
    }};

    var student2 = new HashMap<String, Object>() {{
      put("studentFirstName", "George");
      put("studentLastName", "Washington Carver");
    }};

    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("students", List.of(student1, student2))
    )).build();

    var items = SubmissionUtilities.getDocUploadIdentityStudentsList(submission);
    assertThat(items.get(0)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("name", "Sally A Starfish"),
      Map.entry("isApplicant", "false")
    ));
    assertThat(items.get(1)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("name", "George Washington Carver"),
      Map.entry("isApplicant", "false")
    ));
  }

  @Test
  void docUploadIdentityStudentsListContainsApplicantIfNecessary() {
    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("isApplyingForSelf", "true")
    )).build();

    var items = SubmissionUtilities.getDocUploadIdentityStudentsList(submission);
    assertThat(items.get(0)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("name", "Johnny Appleseed"),
      Map.entry("isApplicant", "true")
    ));
  }

  @Test
  void docUploadEnrollmentStudentsListContainsStudents() {
    var student1 = new HashMap<String, Object>() {{
      put("studentFirstName", "Sally");
      put("studentMiddleInitial", "A");
      put("studentLastName", "Starfish");
      put("studentSchoolType", "virtual");
    }};

    var student2 = new HashMap<String, Object>() {{
      put("studentFirstName", "George");
      put("studentLastName", "Washington Carver");
      put("studentSchoolType", "homeschool");
      put("studentHomeschoolAffidavitNumber", "ABC1234");
    }};

    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("students", List.of(student1, student2))
    )).build();

    var items = SubmissionUtilities.getDocUploadEnrollmentStudentsList(submission);
    assertThat(items.size()).isEqualTo(1);
    assertThat(items.get(0)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("name", "Sally A Starfish"),
      Map.entry("isApplicant", "false")
    ));
  }

  @Test
  void docUploadUnearnedIncludesIncomeTypes() {
    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("incomeUnearnedRetirementTypes[]", List.of("incomeSocialSecurity", "incomeSSI", "income401k403b", "incomePension")),
      Map.entry("incomeUnearnedTypes[]", List.of("incomeUnemployment", "incomeWorkersCompensation", "incomeSpousalSupport", "incomeChildSupport", "incomeDisability", "incomeVeterans", "incomeOther")),
      Map.entry("incomeSSIAmount", "123"),
      Map.entry("incomeWorkersCompensationAmount", "456")
    )).build();

    var items = SubmissionUtilities.getDocUploadUnearnedIncomeList(submission);
    assertThat(items.size()).isEqualTo(11);
    assertThat(items.get(1)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("type", "incomeSSI"),
      Map.entry("amount", "$123")
    ));
    assertThat(items.get(5)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("type", "incomeWorkersCompensation"),
      Map.entry("amount", "$456")
    ));
  }

  @Test
  void docUploadUnearnedSkipsNone() {
    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("incomeUnearnedRetirementTypes[]", List.of("none")),
      Map.entry("incomeUnearnedTypes[]", List.of("none"))
    )).build();

    var items = SubmissionUtilities.getDocUploadUnearnedIncomeList(submission);
    assertThat(items.size()).isEqualTo(0);
  }

  @Test
  void studentsEligible() {
    var student1 = new HashMap<String, Object>() {{
      put("studentFirstName", "Sally");
      put("studentMiddleInitial", "A");
      put("studentLastName", "Starfish");
      put("studentUnenrolledSchoolName", "37680230135277 - Muraoka (Saburo) Elementary (Chula Vista Elementary)");
    }};
    var student2 = new HashMap<String, Object>() {{
      put("studentFirstName", "Rodger");
      put("studentLastName", "Rocklobster");
      put("studentUnenrolledSchoolName", "58727366056790 - Yuba Gardens Intermediate (Marysville Joint Unified)");
    }};

    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("students", List.of(student1, student2))
    )).build();

    assertThat(SubmissionUtilities.allStudentsEligible(submission)).isTrue();
  }

  @Test
  void studentsEligibleWithOneCustom() {
    var student1 = new HashMap<String, Object>() {{
      put("studentFirstName", "Sally");
      put("studentMiddleInitial", "A");
      put("studentLastName", "Starfish");
      put("studentUnenrolledSchoolName", "37680230135277 - Muraoka (Saburo) Elementary (Chula Vista Elementary)");
    }};
    var student2 = new HashMap<String, Object>() {{
      put("studentFirstName", "Rodger");
      put("studentLastName", "Rocklobster");
      put("studentUnenrolledSchoolName", "Custom School Name");
    }};

    var submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Johnny"),
      Map.entry("lastName", "Appleseed"),
      Map.entry("students", List.of(student1, student2))
    )).build();

    assertThat(SubmissionUtilities.allStudentsEligible(submission)).isFalse();
  }
}
