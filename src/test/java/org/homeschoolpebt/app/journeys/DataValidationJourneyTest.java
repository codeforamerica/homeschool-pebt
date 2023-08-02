package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;

public class DataValidationJourneyTest extends AbstractBasePageTest {
  @Test
  void testRedirectingHome() {
    // Landing screen
    assertPageTitle("Get food money for students TK-12.");
    testPage.clickButton("Apply now");
    // How this works
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    driver.navigate().to("http://localhost:%s/flow/pebt/studentsSignpost".formatted(localServerPort));
    assertPageTitle("Get food money for students TK-12.");
  }
}
