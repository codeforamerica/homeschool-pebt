package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;

public class LaterDocsJourneyTest extends AbstractBasePageTest {

  @Test
  void fullLaterDocs() {
    // Homepage
    assertPageTitle("Get food money for students.");
    testPage.clickLink("Submit documents");
    // Add documents signpost
    testPage.clickButton("Get started");

    // Upload docs screen
    assertPageTitle("Upload documents");
    uploadJpgFile("docUpload");
    testPage.clickContinue();

    // Success!
    assertPageTitle("Success");
  }
}
