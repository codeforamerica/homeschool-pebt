package org.homeschoolpebt.app.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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
}
