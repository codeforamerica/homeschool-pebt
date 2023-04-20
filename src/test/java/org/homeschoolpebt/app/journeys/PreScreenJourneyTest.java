package org.homeschoolpebt.app.journeys;

import static org.assertj.core.api.Assertions.assertThat;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;

public class PreScreenJourneyTest extends AbstractBasePageTest {

  @Test
  void preScreenIneligible() {
    // Landing screen
    assertThat(testPage.getTitle()).isEqualTo("Apply for UBI payments easily online.");
    testPage.clickButton("Apply now");
    // How this works
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    testPage.clickButton("Yes"); // More than 1 student?
    testPage.clickButton("No"); // Applying for self?
    testPage.clickButton("No"); // Enrolled in virtual/home school?

    assertThat(testPage.getTitle()).contains("Sorry");
    testPage.clickLink("< Go Back");
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?

    testPage.clickButton("No"); // Unenrolled during COVID?
    assertThat(testPage.getTitle()).contains("Sorry");
    testPage.clickLink("< Go Back");
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?

    assertThat(testPage.getTitle()).doesNotContain("Sorry");
  }
}
