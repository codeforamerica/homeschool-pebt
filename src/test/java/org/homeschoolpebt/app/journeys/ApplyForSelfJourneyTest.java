package org.homeschoolpebt.app.journeys;

import com.mailgun.model.message.MessageResponse;
import com.twilio.rest.api.v2010.account.Message;
import formflow.library.email.MailgunEmailClient;
import org.homeschoolpebt.app.data.SentMessageRepositoryService;
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
import static org.homeschoolpebt.app.utils.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ApplyForSelfJourneyTest extends AbstractBasePageTest {
  @MockBean
  MailgunEmailClient mailgunEmailClient;
  @MockBean
  SentMessageRepositoryService sentMessageRepositoryService;
  @MockBean
  TwilioSmsClient twilioSmsClient;

  @Test
  void selfApplyFullFlow() {
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
    testPage.clickButton("Yes"); // Applying for self?
    assertPageTitle("During the 2022-2023 school year, were you enrolled in a California-based");
    testPage.clickButton("Yes");

    assertPageTitle("Did you withdraw from an in-person school anytime after January 27, 2020?");
    testPage.clickButton("Yes");
    assertPageTitle("Which school did you leave after January 27, 2020?");
    WebElement comboboxMenu = testPage.findElementById("ui-id-1");
    testPage.findElementById("combobox").sendKeys("S.F. County Special"); // Not CEP
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
    testPage.findElementById("studentGrade").sendKeys("4th Grade");
    testPage.findElementById("studentSchoolType-homeschool").click();
    testPage.findElementById("studentHomeschoolAffidavitNumber").sendKeys("abc1234");
    testPage.clickContinue();
    testPage.clickContinue(); // Which school did <name> unenroll from?
    testPage.clickContinue(); // Which school would <name> attend?
    assertPageTitle("Are these all the students");
    testPage.findElementsByClass("subflow-list__action-delete").get(0).click();
    assertPageTitle("Delete student");
    testPage.clickLink("No, keep them on the application");
    testPage.clickButton("Yes, this is everyone");

    assertPageTitle("Getting to know you");
    testPage.clickContinue();

    // Personal info
    testPage.enter("firstName", "Stud");
    testPage.enter("lastName", "McStudenty");
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
    // TODO: Test of skipping the Household builder
    testPage.clickButton("Yes"); // Are there other people in the students' household?
    testPage.clickButton("Get started"); // Household details signpost

    // Housemate Info
    assertPageTitle("Housemate Info");
    testPage.enter("householdMemberFirstName", "John");
    testPage.enter("householdMemberLastName", "Doe");
    testPage.clickContinue();
    // Household List
    testPage.clickButton("+ Add a person");
    // Housemate Info
    testPage.enter("householdMemberFirstName", "Jane");
    testPage.enter("householdMemberLastName", "Doe");
    testPage.clickContinue();
    // Two household members are present
    assertThat(testPage.getCssSelectorText(".form-card__content")).contains("John Doe");
    assertThat(testPage.getCssSelectorText(".form-card__content")).contains("Jane Doe");
    // Delete Jane Doe
    testPage.findElementsByClass("subflow-delete").get(1).click();
    testPage.clickButton("Yes, remove them");
    assertThat(testPage.getCssSelectorText(".form-card__content")).doesNotContain("Jane Doe");
    assertThat(testPage.findElementsByClass("subflow-delete")).hasSize(1);
    // Go back to delete confirmation and make sure someone else isn't deleted
    testPage.goBack();
    assertThat(testPage.getHeader()).contains("This entry has already been deleted");
    testPage.clickButton("Return to the screen I was on before");
    assertThat(testPage.getHeader()).contains("Is this everyone in the student's household?");
    assertThat(testPage.findElementsByClass("subflow-delete")).hasSize(1);
    // Delete final household member to go back to householdList
    testPage.findElementsByClass("subflow-delete").get(0).click();
    testPage.clickButton("Yes, remove them");
    assertPageTitle("Are there other people in your household?");
    // Add back household members
    testPage.enter("hasHousehold", YES.getDisplayValue());
    testPage.clickButton("Get started"); // Household details signpost
    // Housemate Info
    testPage.enter("householdMemberFirstName", "John");
    testPage.enter("householdMemberLastName", "Doe");
    testPage.clickContinue();
    // Household List
    testPage.clickButton("+ Add a person");
    // Housemate Info
    testPage.enter("householdMemberFirstName", "Jane");
    testPage.enter("householdMemberLastName", "Doe");
    testPage.clickContinue();
    // Edit a person
    assertPageTitle("Is this everyone in the student's household?");
    testPage.findElementsByClass("subflow-edit").get(0).click();
    assertPageTitle("Housemate Info");
    testPage.enter("householdMemberFirstName", "Anthony");
    testPage.enter("householdMemberLastName", "Dee");
    testPage.clickContinue();
    assertThat(testPage.getCssSelectorText(".form-card__content")).contains("Anthony Dee");
    assertThat(testPage.getCssSelectorText(".form-card__content")).doesNotContain("John Doe");
    testPage.clickButton("Yes, this is everyone");
    // Anyone receive benefits?
    // TODO: Add test where we select CalFresh to skip income verification
    assertPageTitle("Does anyone in the student's household receive one of these benefits?");
    testPage.findElementById("householdMemberReceivesBenefits-None of the Above").click();
    testPage.clickContinue();

    // Income
    testPage.clickButton("Get started"); // Income signpost
    assertPageTitle("Do you have a job?");
    testPage.clickButton(NO.getDisplayValue());
    // Skips earned income
    testPage.findElementById("incomeUnearnedRetirementTypes-incomeSocialSecurity").click(); // Does anyone get retirement income?
    testPage.clickButton("Submit");
    testPage.findElementById("incomeUnearnedTypes-incomeChildSupport").click(); // Does anyone get unearned income i.e. benefits income?
    testPage.clickButton("Submit");
    testPage.enter("incomeSocialSecurityAmount", "123"); // Tell us how much you made from unearned sources?
    testPage.enter("incomeChildSupportAmount", "456");
    testPage.clickContinue();

    testPage.clickLink("Next step"); // Done (with income)! Let's get your application submitted.

    // Document Uploader
    assertPageTitle("Adding Documents");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Students' proof of identity");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Proof of virtual school enrollment");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Proof of income");
    testPage.clickButton("Get started");
    testPage.clickButton("Got it"); // How to add files from your device
    assertPageTitle("Add proof of identity");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Stud McStudenty");
    uploadJpgFile("identityFiles");
    testPage.clickContinue();
    assertPageTitle("Add proof of virtual school enrollment");
    // TODO: Fix that the enrollment docs are showing up
    // https://app.asana.com/0/1204253048942731/1204911485797535
    assertPageTitle("Add proof of virtual school enrollment");
    testPage.clickLink("Skip");
    assertPageTitle("Add proof for other income sources");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Social Security");
    uploadJpgFile("unearnedIncomeFiles");
    testPage.clickContinue();
    assertPageTitle("We'll need the rest of your documents");
    testPage.clickContinue();

    // Submitting your application
    testPage.clickButton("Get started");
    testPage.clickElementById("agreesToLegalTerms-true-label");
    testPage.clickContinue();
    testPage.enter("signature", "Anything");
    testPage.clickButton("Submit Application");
    verify(mailgunEmailClient, times(1)).sendEmail(
      eq("Documents Needed for P-EBT 4.0 Application"),
      eq("foo@test.com"),
      contains("Thank you for beginning the application for P-EBT benefits."));
    verify(twilioSmsClient, times(1)).sendMessage(
      eq("(312) 877-1021"),
      contains("Thank you for beginning the application for P-EBT benefits."));
    verify(sentMessageRepositoryService).save(
      argThat(sm -> sm.getProvider().equals("twilio"))
    );
    verify(sentMessageRepositoryService).save(
      argThat(sm -> sm.getProvider().equals("mailgun"))
    );
  }
}
