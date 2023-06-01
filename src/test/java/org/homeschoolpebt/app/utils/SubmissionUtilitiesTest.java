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

    assertEquals(SubmissionUtilities.getHourlyGrossIncomeAmount(job), "$180");
  }

  @Test
  void hourlyGrossIncomeDescriptionIsCorrect() {
    Map<String, Object> job = new HashMap<>() {{
      put("incomeIsJobHourly", "true");
      put("incomeHoursPerWeek", "10");
      put("incomeHourlyWage", "18");
    }};

    assertEquals(SubmissionUtilities.getHourlyGrossIncomeExplanation(job), "10.0 hours * $18 per hour");
  }
}
