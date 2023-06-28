package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.homeschoolpebt.app.utils.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("staticPagesJourney")
public class StaticPagesJourneyTest extends AbstractBasePageTest {

  protected void initTestPage() {
    testPage = new Page(driver);
  }

  @Test
  void staticPagesJourney() {
    var windowHandle = driver.getWindowHandle();
    // Landing screen
    assertPageTitle("Get food money for students TK-12.");
    // Go to privacy policy tab
    testPage.clickLink("Privacy Policy");
    switchAwayFromOriginalWindow(windowHandle);
    assertPageTitle("Privacy Policy");
  }

  void switchAwayFromOriginalWindow(String originalWindow) {
    for (String windowHandle : driver.getWindowHandles()) {
      if(!originalWindow.contentEquals(windowHandle)) {
        driver.switchTo().window(windowHandle);
        break;
      }
    }
  }
}
