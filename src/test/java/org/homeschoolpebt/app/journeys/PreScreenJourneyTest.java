package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;

public class PreScreenJourneyTest extends AbstractBasePageTest {
  @Test
  void preScreenEligible() {
    assertPageTitle("Get food money for students TK-12.");
    testPage.clickButton("Apply now");
    // Application Steps
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    testPage.clickButton("Yes"); // More than 1 student?
    testPage.clickButton("Yes"); // Were 1 or more students in TK-2?
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?

    assertPageTitle("Which school"); // Which school did the 1 or more of the students withdraw from...
  }

  @Test
  void preScreenIneligible() {
    // Landing screen
    assertPageTitle("Get food money for students TK-12.");
    testPage.clickButton("Apply now");
    // Application Steps
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    assertPageTitle("more than 1 student?");
    testPage.clickButton("Yes"); // More than 1 student?

    // Enrolled in virtual/home school?
    assertPageTitle("were 1 or more students enrolled");
    testPage.clickButton("No");
    assertPageTitle("Sorry");
    testPage.goBack();
    testPage.clickButton("Yes");

    // Withdrawn from school?
    assertPageTitle("withdraw from an in-person school");
    testPage.clickButton("No");
    assertPageTitle("in grade TK-2");
    testPage.clickButton("No");
    assertPageTitle("Sorry");
    testPage.goBack();
    testPage.clickButton("Yes");
    assertPageTitle("might be eligible");
    testPage.goBack(); // TK-2
    testPage.goBack();
    testPage.clickButton("Yes"); // Withdrawn from school

    assertPageTitle("Which school"); // Which school did the 1 or more of the students withdraw from...
  }
}
