package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;

public class PreScreenJourneyTest extends AbstractBasePageTest {

  @Test
  void preScreenIneligible() {
    // Landing screen
    assertPageTitle("Get food money for students TK-12.");
    testPage.clickButton("Apply now");
    // How this works
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    testPage.clickButton("Yes"); // More than 1 student?
    assertPageTitle("were 1 or more students enrolled");
    testPage.clickLink("< Go Back");
    testPage.clickButton("No"); // More than 1 student?

    testPage.clickButton("No"); // Applying for self?
    testPage.clickButton("No"); // Enrolled in virtual/home school?

    assertPageTitle("Sorry");
    testPage.clickLink("< Go Back");
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?

    testPage.clickButton("No"); // Unenrolled during COVID?
    assertPageTitle("Sorry");
    testPage.clickLink("< Go Back");
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?

    assertPageTitle("Which school"); // Which school did the 1 or more of the students withdraw from...
  }
}
