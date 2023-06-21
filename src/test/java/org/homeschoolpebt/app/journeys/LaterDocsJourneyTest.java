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

    // Application Number Page
    testPage.enter("firstName", "John");
    testPage.enter("lastName", "Doe");
    testPage.enter("applicationNumber", "1234567");
    testPage.clickContinue();

    // How to
    testPage.clickButton("Got it");

    // Upload docs screen
    assertPageTitle("Upload documents");
    uploadJpgFile("docUpload");
    testPage.clickButton("I'm finished uploading");

    // Confirmation screen
    assertPageTitle("Ready to submit your documents");
    testPage.clickButton("Yes, submit and finish");

    // Success!
    assertPageTitle("Success");
  }
}
