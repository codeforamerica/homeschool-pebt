package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.inputs.Pebt;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubmissionUtilitiesTest {
  @Test
  void testFromSubmission() {
    HashMap<String, Object> job1 = new HashMap<>() {{
      put("incomeMember", "Johnny Potato");
      put("incomeJobName", "Tuber");
      put("incomeWillBeLess", "true");
      put("incomeSelfEmployed", "true");
      put("incomeCustomAnnualIncome", "1200"); // Future: $100/mo.
      put("incomeGrossMonthlyIndividual", "200"); // Past: $200 monthly gross - 40% standard = $120 net
      put("incomeWillBeLessDescription", "I will be planting fewer potatoes.");
    }};

    HashMap<String, Object> student1 = new HashMap<>() {{
      put("studentFirstName", "Tater");
      put("studentLastName", "Masher");
      put("studentDesignations[]", List.of("foster", "runaway"));
    }};

    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Spud"),
      Map.entry("lastName", "Tuberville"),
      Map.entry("income", List.of(job1)),
      Map.entry("students", List.of(student1)),
      Map.entry("incomeTypes[]", List.of(Pebt.INCOME_TYPES.incomeUnemployment)),
      Map.entry("incomeUnemploymentAmount", "111")
    )).build();

    var pebt = Pebt.fromSubmission(submission);
    assertEquals(pebt.getFirstName(), "Spud");
    assertEquals(pebt.getLastName(), "Tuberville");
    assertThat(pebt.getIncomeTypes()).contains(Pebt.INCOME_TYPES.incomeUnemployment);
    assertThat(pebt.getStudents().get(0).getStudentFirstName()).isEqualTo("Tater");
    assertThat(pebt.getStudents().get(0).getStudentLastName()).isEqualTo("Masher");
  }

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
    var job = new Pebt.Income() {{
      setIncomeIsJobHourly("true");
      setIncomeHoursPerWeek("10");
      setIncomeHourlyWage("18");
    }};

    assertEquals(SubmissionUtilities.getHourlyGrossIncomeAmount(job), 180.0);
  }

  @Test
  void hourlyGrossIncomeDescriptionIsCorrect() {
    var job = new Pebt.Income() {{
      setIncomeIsJobHourly("true");
      setIncomeHoursPerWeek("10");
      setIncomeHourlyWage("18");
    }};

    assertEquals(SubmissionUtilities.getHourlyGrossIncomeExplanation(job), "10.0 hours * $18 per hour");
  }

  @Test
  void regularPayAmountIsCorrect() {
    // $400 * weekly = $400 * 52 / 12 = $1733.33 monthly
    var jobWeekly = new Pebt.Income() {{
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("weekly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobWeekly), 1733.3333333333333333333);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobWeekly), "$400 every week");

    // $400 * every 2 weeks = $400 * 26 / 12 = $866.67 monthly
    var jobBiweekly = new Pebt.Income() {{
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("biweekly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobBiweekly), 866.6666666666666666);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobBiweekly), "$400 every 2 weeks");

    // $400 * twice a month = $400 * 24 / 12 = $800
    var jobSemimonthly = new Pebt.Income() {{
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("semimonthly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobSemimonthly), 800.0);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobSemimonthly), "$400 twice a month");

    // $400 * monthly = $400
    var jobMonthly = new Pebt.Income() {{
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("monthly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobMonthly), 400.0);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobMonthly), "$400 monthly");

    // $400 * seasonally = $400 / 12 = $33.33
    var jobSeasonally = new Pebt.Income() {{
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("seasonally");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobSeasonally), 33.333333333333333333);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobSeasonally), "$400 seasonally");

    // $400 * yearly = $400 / 12 = $33.33
    var jobYearly = new Pebt.Income() {{
      setIncomeIsJobHourly("false");
      setIncomeRegularPayAmount("400");
      setIncomeRegularPayInterval("yearly");
    }};
    assertEquals(SubmissionUtilities.getRegularPayAmount(jobYearly), 33.3333333333333333);
    assertEquals(SubmissionUtilities.getRegularPayExplanation(jobYearly), "$400 yearly");
  }
}
