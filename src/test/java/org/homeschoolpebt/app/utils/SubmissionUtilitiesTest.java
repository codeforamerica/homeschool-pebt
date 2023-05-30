package org.homeschoolpebt.app.utils;

import static org.junit.jupiter.api.Assertions.*;

import formflow.library.data.Submission;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
    assertEquals("", SubmissionUtilities.formatMoney(null));
  }

  @Test
  @Disabled("Before re-enabling comma handling, ensure commas show up properly in incomeSelfEmployedWillBeLess.html")
  void formatMoneyAddsCommaForThousands() {
    assertEquals("$1,000", SubmissionUtilities.formatMoney("1000"));
  }
}