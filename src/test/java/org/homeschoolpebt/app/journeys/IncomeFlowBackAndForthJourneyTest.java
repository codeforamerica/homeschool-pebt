package org.homeschoolpebt.app.journeys;

import com.mailgun.model.message.MessageResponse;
import com.twilio.rest.api.v2010.account.Message;
import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import formflow.library.data.SubmissionRepositoryService;
import formflow.library.email.MailgunEmailClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.homeschoolpebt.app.submission.messages.TwilioSmsClient;
import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.homeschoolpebt.app.utils.YesNoAnswer.NO;
import static org.homeschoolpebt.app.utils.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class IncomeFlowBackAndForthJourneyTest extends AbstractBasePageTest {
  @MockBean
  MailgunEmailClient mailgunEmailClient;
  @MockBean
  TwilioSmsClient twilioSmsClient;

  @Autowired
  private SubmissionRepositoryService submissionRepositoryService;

  @PersistenceContext
  EntityManager entityManager;

  @Test
  void test() {
    beforeIncome();
    // Income
    testPage.clickButton("Get started"); // Income signpost
    assertPageTitle("Does anyone in the household have a job?");
    testPage.clickButton(YES.getDisplayValue());
    assertPageTitle("Great! Let's add all the jobs in the household.");
    testPage.clickButton("Add a job");
    assertPageTitle("Who do you want to add the job for?");
    testPage.findElementByCssSelector("input[type=radio]").click(); // Click first radio button
    testPage.clickContinue();
    assertPageTitle("Add a job for Testy McTesterson");
    testPage.enter("incomeJobName", "Hobby Jobby"); // Name of job
    testPage.clickButton("Continue");

    var goBackCount = 0;
    testPage.clickButton(NO.getDisplayValue()); // Was self-employed?
    testPage.clickButton(YES.getDisplayValue()); // Is this job paid by the hour?
    goBackCount++;
    // First say it's hourly
    testPage.enter("incomeHourlyWage", "10"); // What's [x]'s hourly wage?
    testPage.enter("incomeHoursPerWeek", "40");
    testPage.clickContinue();
    // Then change your mind
    goBackCount++;
    for (var i = 0; i < goBackCount; i++) {
      testPage.goBack();
    }
    assertPageTitle("Is this job paid by the hour?");
    testPage.clickButton(NO.getDisplayValue()); // Is this job paid by the hour?
    assertPageTitle("How does Testy McTesterson get paid?");
    // Expect data fields to remain for now
    expectIncomeField("incomeIsJobHourly", "false");
    expectIncomeField("incomeHourlyWage", "10");
    expectIncomeField("incomeHoursPerWeek", "40");
    testPage.findElementById("incomeRegularPayInterval-monthly-label").click();
    testPage.enterByCssSelector("input:not([disabled])#incomeRegularPayAmount", "300");
    testPage.clickContinue();
    // Expect data fields to remain for now
    expectIncomeField("incomeIsJobHourly", "false");
    expectIncomeField("incomeRegularPayAmount", "300");
    // fail below
    expectIncomeField("incomeHoursPerWeek", "");
    expectIncomeField("incomeHourlyWage", "");
    if (true) { return; }
    assertPageTitle("Do you think Testy McTesterson will make less from this job in future months?");
    testPage.goBack();
    testPage.goBack();
    testPage.clickButton(NO.getDisplayValue()); // Is this job paid by the hour?
    testPage.findElementById("incomeRegularPayInterval-semimonthly-label").click(); // How does [x] get paid?
    testPage.enterByCssSelector("#follow-up-semimonthly input[name='incomeRegularPayAmount']", "1000");
    testPage.clickContinue();
    testPage.findElementById("incomeWillBeLess-true-label").click(); // Will income be less?
    testPage.enter("incomeCustomMonthlyIncome", "500");
    testPage.enter("incomeWillBeLessDescription", "Some string about why income will be less.");
    testPage.clickContinue();
    assertPageTitle("Great! Any other jobs in the household to add?");
    testPage.clickButton("I'm done adding jobs");
    testPage.clickLink("Keep going"); // Almost done with income!
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
    testPage.clickButton("Get started");
    testPage.clickButton("Got it"); // How to add files from your device
    assertPageTitle("Add proof of identity");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Stud McStudenty");
    uploadJpgFile("identityFiles");
    testPage.clickContinue();
    // Skip 'Add proof of virtual school enrollment' (since an affidavit number was provided before)
    assertPageTitle("Add proof of income from the last 30 days");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Testy McTesterson (that's you!)");
    uploadJpgFile("incomeFiles");
    testPage.clickContinue();
    assertPageTitle("Add proof for other income sources");
    assertThat(testPage.getCssSelectorText(".boxed-content")).contains("Social Security");
    uploadJpgFile("unearnedIncomeFiles");
    testPage.clickContinue();
    assertPageTitle("Doc submit confirmation");
    testPage.clickButton("Yes, submit and finish");

    // Submitting your application
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

  private void beforeIncome() {

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
    testPage.clickButton("Yes"); // More than 1 student?
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?
    testPage.clickButton("Yes"); // Unenrolled during COVID?

    assertPageTitle("Which school did 1 or more of the students withdraw from after January 27, 2020?");
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
    testPage.clickContinue(); // Which school would <name> attend?
    assertPageTitle("Are these all the students");
    testPage.findElementsByClass("subflow-list__action-delete").get(0).click();
    assertPageTitle("Delete student");
    testPage.clickLink("No, keep them on the application");
    testPage.clickButton("Yes, this is everyone");
    assertPageTitle("Are you in the same household as these students?");
    testPage.clickButton("Yes"); // Are you in the same household as the students?
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
    assertPageTitle("Are there other people in the students' household?");
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
  }

  private void expectIncomeField(String fieldName, Object value) {
    var query = entityManager.createQuery("SELECT s FROM Submission s order by createdAt desc limit 1");
    var submission = (Submission) query.getSingleResult();
    var inputData = submission.getInputData();
    assertThat(inputData).isNotNull();
    var incomeList = (List<HashMap<String, Object>>) inputData.getOrDefault("income", null);
    assertThat(incomeList).isNotNull();
    var income = incomeList.get(0);
    assertThat(income).isNotNull();
    Object _missing = new Object();
    var actual = income.getOrDefault(fieldName, _missing);
    if (value == null) {
      assertThat(actual).isEqualTo(_missing);
    } else {
      assertThat(actual).isNotEqualTo(_missing);
      assertThat(actual).isEqualTo(value);
    }
  }
}
