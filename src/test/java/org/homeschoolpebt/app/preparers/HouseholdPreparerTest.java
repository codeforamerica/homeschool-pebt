package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HouseholdPreparerTest {
  @Test
  void addsHouseholdMembers() {
    HashMap<String, String> student = new HashMap<>() {{
      put("studentFirstName", "Bobby");
      put("studentMiddleInitial", "B");
      put("studentLastName", "Banana");
    }};

    HashMap<String, String> hhmember = new HashMap<>() {{
      put("householdMemberFirstName", "Charlie");
      put("householdMemberLastName", "Cantaloupe");
    }};

    HashMap<String, Object> job1 = new HashMap<>() {{
      put("incomeMember", "Avery Applicant");
      put("incomeJobName", "Avery Day Im Hustlin'");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeCustomAnnualIncome", "1200"); // Future: $100/mo.
      put("incomeGrossMonthlyIndividual", "200"); // Past: $200 monthly gross - 40% standard = $120 net
      put("incomeWillBeLessDescription", "I will be hustling less.");
      put("iterationIsComplete", true);
    }};

    // Self Employment w/Custom Deductions
    HashMap<String, Object> job2 = new HashMap<>() {{
      put("incomeMember", "Charlie Cantaloupe");
      put("incomeJobName", "Gourd Progressions");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeSelfEmployedCustomOperatingExpenses", "true");
      put("incomeSelfEmployedOperatingExpenses", "100");
      put("incomeCustomAnnualIncome", "600"); // Future: $50/mo.
      put("incomeGrossMonthlyIndividual", "200"); // $200 monthly gross - $100 custom operating expenses = $100 net
      put("incomeWillBeLessDescription", "My operating expenses are very high.");
      put("iterationIsComplete", true);
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("applicantIsInHousehold", "true"),
      Map.entry("firstName", "Avery"),
      Map.entry("lastName", "Applicant"),
      Map.entry("students", List.of(student)),
      Map.entry("household", List.of(hhmember)),
      Map.entry("income", List.of(job1, job2))
    )).build();

    HouseholdPreparer preparer = new HouseholdPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("hhmember1", new SingleField("hhmember1", "Avery Applicant", null)),
      Map.entry("hhmember1-future-income", new SingleField("hhmember1-future-income", "$100", null)),
      Map.entry("hhmember1-student", new SingleField("hhmember1-student", "No", null)),
      Map.entry("hhmember2", new SingleField("hhmember2", "Bobby B Banana", null)),
      Map.entry("hhmember2-future-income", new SingleField("hhmember2-future-income", "$0", null)),
      Map.entry("hhmember2-student", new SingleField("hhmember2-student", "Yes", null)),
      Map.entry("hhmember3", new SingleField("hhmember3", "Charlie Cantaloupe", null)),
      Map.entry("hhmember3-future-income", new SingleField("hhmember3-future-income", "$50", null)),
      Map.entry("hhmember3-student", new SingleField("hhmember3-student", "No", null))
    ));
  }

  @Test
  void excludesSubmitterWhenNotInHousehold() {
    HashMap<String, String> student = new HashMap<>() {{
      put("studentFirstName", "Bobby");
      put("studentMiddleInitial", "B");
      put("studentLastName", "Banana");
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("applicantIsInHousehold", "false"),
      Map.entry("firstName", "Avery"),
      Map.entry("lastName", "Applicant"),
      Map.entry("students", List.of(student))
    )).build();

    HouseholdPreparer preparer = new HouseholdPreparer();
    var fields = preparer.prepareSubmissionFields(submission, null);
    assertThat(fields).containsAllEntriesOf(Map.ofEntries(
      Map.entry("hhmember1", new SingleField("hhmember1", "Bobby B Banana", null)),
      Map.entry("hhmember1-future-income", new SingleField("hhmember1-future-income", "$0", null)),
      Map.entry("hhmember1-student", new SingleField("hhmember1-student", "Yes", null))
    ));
    assertThat(fields).doesNotContainKeys("hhmember2", "hhmember2-future-income", "hhmember2-student");
  }
}
