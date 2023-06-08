package org.homeschoolpebt.app.journeys;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.homeschoolpebt.app.utils.YesNoAnswer.NO;
import static org.homeschoolpebt.app.utils.YesNoAnswer.YES;

public class PebtFlowJourneyTest extends AbstractBasePageTest {

  @Test
  void fullUbiFlow() {
    // Landing screen
    assertThat(testPage.getTitle()).isEqualTo("Get food money for students.");
    testPage.clickButton("Apply now");
    // How this works
    testPage.clickContinue();

    // Pre-screen
    testPage.clickButton("Ok, I'm ready");
    testPage.clickButton("Yes"); // More than 1 student?
    testPage.clickButton("Yes"); // Enrolled in virtual/home school?
    testPage.clickButton("Yes"); // Unenrolled during COVID?

    assertThat(testPage.getTitle()).isEqualTo("Which school did 1 or more of the students withdraw from after January 27, 2020?");
    WebElement comboboxMenu = testPage.findElementById("ui-id-1");
    testPage.findElementById("combobox").sendKeys("San Franc");
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
    assertThat(testPage.getTitle()).contains("Add a student");
    testPage.enter("studentFirstName", "Stud");
    testPage.enter("studentLastName", "McStudenty");
    testPage.enter("studentBirthdayDay", "1");
    testPage.enter("studentBirthdayMonth", "2");
    testPage.enter("studentBirthdayYear", "1991");
    testPage.clickButton("Add student");
    assertThat(testPage.getTitle()).contains("Do any of the following apply to Stud?");
    testPage.findElementById("none__checkbox").click();
    testPage.clickContinue();
    assertThat(testPage.getTitle()).contains("Tell us about");
    testPage.findElementById("studentGrade").sendKeys("4th Grade");
    testPage.findElementById("studentSchoolType-homeschool").click();
    testPage.findElementById("studentHomeschoolAffidavitNumber").sendKeys("abc1234");
    testPage.clickContinue();
    testPage.clickContinue(); // Which school did <name> unenroll from?
    testPage.clickContinue(); // Which school would <name> attend?
    assertThat(testPage.getTitle()).contains("Are these all the students");
    testPage.findElementsByClass("subflow-list__action-delete").get(0).click();
    assertThat(testPage.getTitle()).contains("Delete student");
    testPage.clickLink("No, keep them on the application");
    testPage.clickButton("Yes, this is everyone");
    assertThat(testPage.getTitle()).contains("Are you in the same household as these students?");
    testPage.clickButton("Yes"); // Are you in the same household as the students?

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
    assertThat(testPage.getTitle()).isEqualTo("Contact Info");
    testPage.enter("phoneNumber", "(312) 877-1021");
    testPage.enter("email", "foo@test.com");
    // Assert JavaScript is checking the phone and email checkboxes when values are entered
    assertThat(testPage.findElementById("howToContactYou-phoneNumber").isSelected()).isTrue();
    assertThat(testPage.findElementById("howToContactYou-email").isSelected()).isTrue();
    testPage.clickContinue();
    // Review personal info
    assertThat(testPage.getTitle()).isEqualTo("Let's review your information");
    testPage.clickButton("Confirm");
    // TODO: Test of skipping the Household builder
    testPage.clickButton("Yes"); // Are there other people in the students' household?
    testPage.clickButton("Get started"); // Household details signpost

    // Housemate Info
    assertThat(testPage.getTitle()).isEqualTo("Housemate Info");
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
    assertThat(testPage.getTitle()).isEqualTo("Are there other people in the students' household?");
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
    assertThat(testPage.getTitle()).contains("Is this everyone in the student's household?");
    testPage.findElementsByClass("subflow-edit").get(0).click();
    assertThat(testPage.getTitle()).isEqualTo("Housemate Info");
    testPage.enter("householdMemberFirstName", "Anthony");
    testPage.enter("householdMemberLastName", "Dee");
    testPage.clickContinue();
    assertThat(testPage.getCssSelectorText(".form-card__content")).contains("Anthony Dee");
    assertThat(testPage.getCssSelectorText(".form-card__content")).doesNotContain("John Doe");
    testPage.clickButton("Yes, this is everyone");
    // Anyone receive benefits?
    assertThat(testPage.getTitle()).isEqualTo("Does anyone in the student's household receive one of these benefits?");
    testPage.findElementById("householdMemberReceivesBenefits-CalFresh").click();
    testPage.enter("householdMemberBenefitsCaseNumber", "ABC1234");
    testPage.clickContinue();

    // Income
    testPage.clickButton("Get started"); // Income signpost
    assertThat(testPage.getTitle()).isEqualTo("Does anyone in the household have a job?");
    testPage.clickButton(YES.getDisplayValue());
    assertThat(testPage.getTitle()).isEqualTo("Great! Let's add all the jobs in the household.");
    testPage.clickButton("Add a job");
    assertThat(testPage.getTitle()).isEqualTo("Who do you want to add the job for?");
    testPage.findElementByCssSelector("input[type=radio]").click(); // Click first radio button
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Add a job for Testy McTesterson");
    testPage.enter("incomeJobName", "Hobby Jobby"); // Name of job
    testPage.clickButton("Continue");
    // TODO: Add a case for self-employed income as well.
    testPage.clickButton(NO.getDisplayValue()); // Was self-employed?
    testPage.clickButton(YES.getDisplayValue()); // Is this job paid by the hour?
    testPage.enter("incomeHourlyWage", "10"); // What's [x]'s hourly wage?
    testPage.enter("incomeHoursPerWeek", "40");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Do you think Testy McTesterson will make less from this job in future months?");
    testPage.goBack();
    testPage.goBack();
    testPage.clickButton(NO.getDisplayValue()); // Is this job paid by the hour?
    testPage.findElementById("incomeRegularPayInterval-semimonthly-label").click(); // How does [x] get paid?
    testPage.enter("incomeRegularPayAmount", "1000");
    testPage.clickContinue();
    testPage.findElementById("incomeWillBeLess-true-label").click(); // Will income be less?
    testPage.enter("incomeCustomMonthlyIncome", "500");
    testPage.enter("incomeWillBeLessDescription", "Some string about why income will be less.");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Is this everyone's monthly pay?");
    testPage.clickButton("Yes, that's all the income");
    testPage.clickLink("Keep going"); // Almost done with income!
    testPage.findElementById("incomeUnearnedRetirementTypes-incomeSocialSecurity").click(); // Does anyone get retirement income?
    testPage.clickButton("Submit");
    testPage.findElementById("incomeUnearnedTypes-incomeChildSupport").click(); // Does anyone get unearned income i.e. benefits income?
    testPage.clickButton("Submit");
    // TODO: Restore next line when revising the unearned income amounts page
    testPage.enter("incomeSocialSecurityAmount", "123"); // Tell us how much you made from unearned sources?
    testPage.enter("incomeChildSupportAmount", "456");
    testPage.clickContinue();


    testPage.clickLink("Next step"); // Done (with income)! Let's get your application submitted.
    // TODO: test more income cases and the rest of the flow
  }
}
