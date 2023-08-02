package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


public class FlowSwitchInterceptorJourneyTest extends AbstractBasePageTest {
  @Test
  void testInvalidatingSessionWhenSwitchingFlows() {
    // Landing screen
    assertPageTitle("Get food money for students TK-12");
    // Start pebt flow
    testPage.clickButton("Apply now");
    var firstPebtRequestSessionId = getCurrentSessionCookie();
    // How this works
    testPage.clickContinue();
    var secondPebtRequestSessionId = getCurrentSessionCookie();
    assertThat(firstPebtRequestSessionId).isEqualTo(secondPebtRequestSessionId);
    // Switch to docUpload flow
    driver.navigate().to("http://localhost:%s/flow/docUpload/addDocumentsSignpost".formatted(localServerPort));
    var firstDocUploadRequestSessionId = getCurrentSessionCookie();
    assertThat(firstPebtRequestSessionId).isNotEqualTo(firstDocUploadRequestSessionId);
    testPage.clickLink("Get started");
    var secondDocUploadRequestSessionId = getCurrentSessionCookie();
    assertThat(firstDocUploadRequestSessionId).isEqualTo(secondDocUploadRequestSessionId);
  }

  protected String getCurrentSessionCookie() {
    return driver.manage().getCookieNamed("SESSION").getValue();
  }
}
