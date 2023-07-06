package org.homeschoolpebt.app.journeys;

import com.mailgun.model.message.MessageResponse;
import com.twilio.rest.api.v2010.account.Message;
import formflow.library.email.MailgunEmailClient;
import org.homeschoolpebt.app.submission.messages.TwilioSmsClient;
import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.homeschoolpebt.app.utils.YesNoAnswer.NO;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ApplyForChildOfSingleParentJourneyTest extends AbstractBasePageTest {
  @MockBean
  MailgunEmailClient mailgunEmailClient;
  @MockBean
  TwilioSmsClient twilioSmsClient;

  @Test
  void test() {
    var mockMessageResponse = MessageResponse.builder().id("id").message("message").build();
    when(mailgunEmailClient.sendEmail(any(), any(), any())).thenReturn(mockMessageResponse);
    when(twilioSmsClient.sendMessage(any(), any())).thenReturn(mock(Message.class));

    // Landing screen
    assertPageTitle("Get food money for students TK-12.");
    testPage.clickButton("Apply now");
    // How this works
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    testPage.clickButton("No"); // More than 1 student?
    assertPageTitle("Are you a student applying for P-EBT yourself?");
    testPage.clickButton("No");
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?
    testPage.clickButton("Yes"); // Unenrolled during COVID?

    assertPageTitle("Which school did the student withdraw from after January 27, 2020?");
    WebElement comboboxMenu = testPage.findElementById("ui-id-1");
    testPage.findElementById("combobox").sendKeys("S.F. County Special"); // 38103896069561 - S.F. County Special Education (San Francisco County Office of Education)
    // wait for combobox to appear, then click the item for San Francisco Unified School District
    new WebDriverWait(driver, Duration.ofSeconds(1)).until(ExpectedConditions.visibilityOf(comboboxMenu));
    Optional<WebElement> comboboxItem = comboboxMenu
      .findElements(By.cssSelector(".ui-menu-item"))
      .stream()
      .filter(el -> el.getText().contains("San Francisco"))
      .findFirst();
    assertThat(comboboxItem.isPresent()).isTrue();
    comboboxItem.get().click();
    testPage.clickContinue();
    testPage.clickContinue(); // Nice! You might be eligible!

    // Students subflow
    testPage.clickButton("Get started"); // "Student information" signpost
    assertPageTitle("Add a student");
    testPage.enter("studentFirstName", "Stud");
    testPage.enter("studentLastName", "McStudenty");
    testPage.enter("studentBirthdayDay", "1");
    testPage.enter("studentBirthdayMonth", "2");
    testPage.enter("studentBirthdayYear", "1991");
    testPage.clickButton("Add student");
    assertPageTitle("Do any of the following apply to Stud?");
    testPage.findElementById("none__checkbox").click();
    testPage.clickContinue();
    assertPageTitle("Tell us about");
    testPage.findElementById("studentGrade").sendKeys("1st Grade");
    testPage.findElementById("studentSchoolType-homeschool").click();
    testPage.findElementById("studentHomeschoolAffidavitNumber").sendKeys("abc1234");
    testPage.clickContinue();
    // Test skipping the "Which school did <name> unenroll from?" page for TK-2nd grade
    assertPageTitle("if they were going to school in person"); // Which school would <name> have attended if in person?
    testPage.goBack();
    testPage.findElementById("studentGrade").sendKeys("4th Grade");
    testPage.clickContinue();
    testPage.clickContinue(); // Which school did <name> unenroll from?
    assertPageTitle("Are these all the students");
    testPage.findElementsByClass("subflow-list__action-delete").get(0).click();
    assertPageTitle("Delete student");
    testPage.clickLink("No, keep them on the application");
    testPage.clickButton("Yes, this is everyone");
    assertPageTitle("Are you in the same household as Stud?");
    testPage.clickButton("Yes");
    testPage.clickContinue(); // Got it! Students may be eligible.

    // Getting to know you
    testPage.clickContinue();

    // Personal info
    testPage.enter("firstName", "Testy");
    testPage.enter("lastName", "McTesterson");
    testPage.clickContinue();

    // Home address
    testPage.enter("residentialAddressStreetAddress1", "1111 N State St");
    testPage.enter("residentialAddressStreetAddress2", "Apt 2");
    testPage.enter("residentialAddressCity", "Roswell");
    testPage.enter("residentialAddressState", "NM - New Mexico");
    testPage.enter("residentialAddressZipCode", "88201");
    testPage.clickContinue(); // Address validation gets skipped in test

    // Contact Info
    assertPageTitle("Contact Info");
    testPage.enter("phoneNumber", "(312) 877-1021");
    testPage.enter("email", "foo@test.com");
    testPage.clickContinue();
    // Review personal info
    assertPageTitle("Let's review your information");
    testPage.clickButton("Confirm");
    testPage.clickButton("No"); // Are there other people in the students' household?

    assertPageTitle("Does anyone in the student's household receive one of these benefits?");
    testPage.findElementById("householdMemberReceivesBenefits-None of the Above").click();
    testPage.clickContinue();

    // Income - no income
    testPage.clickButton("Get started"); // Income signpost
    assertPageTitle("Does anyone in the household have a job?");
    testPage.clickButton(NO.getDisplayValue());
    assertPageTitle("Does anyone in the household get retirement money?");
    testPage.findElementById("none__checkbox").click();
    testPage.clickButton("Submit");

    assertPageTitle("Does anyone in the household get money from any of these sources?"); // benefits income
    testPage.findElementById("none__checkbox").click();
    testPage.clickButton("Submit");

    testPage.clickLink("Next step"); // Done (with income)! Let's get your application submitted.

    // Document Uploader
    assertPageTitle("Adding Documents");
    testPage.clickButton("Get started");
    testPage.clickButton("Got it"); // How to add files from your device
    assertPageTitle("Add proof of identity");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Stud McStudenty");
    uploadJpgFile("identityFiles");
    testPage.clickContinue();
    // Skip 'Add proof of virtual school enrollment' (since an affidavit number was provided before)
    // Skip income verification since no income was provided

    // Submitting your application
    assertPageTitle("Doc submit confirmation");
    testPage.clickButton("Yes, submit and finish");
    testPage.clickButton("Get started");
    testPage.clickElementById("agreesToLegalTerms-true-label");
    testPage.clickContinue();
    testPage.enter("signature", "Anything");
    testPage.clickButton("Submit Application");
    verify(mailgunEmailClient, times(1)).sendEmail(
      eq("Application Submitted for P-EBT 4.0"),
      eq("foo@test.com"),
      contains("Thank you for submitting your application for P-EBT benefits for the 2022-2023 school year."));
    verify(twilioSmsClient, times(1)).sendMessage(
      eq("(312) 877-1021"),
      contains("Thank you for submitting the application for P-EBT benefits for the 2022-2023 school year."));

    assertPageTitle("Done! Your application has been submitted to the State of California.");
    testPage.clickElementById("feedbackOption-great-label");
    testPage.enter("feedbackText", "Yay feedback!");
    testPage.clickButton("Submit Feedback");

    assertPageTitle("Thanks for your feedback!");
  }
}
