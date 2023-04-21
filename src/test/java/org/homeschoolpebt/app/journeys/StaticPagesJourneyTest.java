package org.homeschoolpebt.app.journeys;

import static org.assertj.core.api.Assertions.assertThat;

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
    // Landing screen
    assertThat(testPage.getTitle()).isEqualTo("Get food money for students.");
    assertThat(driver.getWindowHandles().size()).isEqualTo(1);
    String originalWindow = driver.getWindowHandle();
    // Go to FAQ tab
    testPage.clickLink("FAQ");
    assertThat(driver.getWindowHandles().size()).isEqualTo(2);
    switchAwayFromOriginalWindow(originalWindow);
    assertThat(testPage.getTitle()).isEqualTo("Frequently Asked Questions");
    // Back on landing screen
    driver.close();
    driver.switchTo().window(originalWindow);
    assertThat(testPage.getTitle()).isEqualTo("Get food money for students.");
    assertThat(driver.getWindowHandles().size()).isEqualTo(1);
    // Go to privacy policy tab
    testPage.clickLink("Privacy Policy");
    assertThat(driver.getWindowHandles().size()).isEqualTo(2);
    switchAwayFromOriginalWindow(originalWindow);
    assertThat(testPage.getTitle()).isEqualTo("Privacy Policy");
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
