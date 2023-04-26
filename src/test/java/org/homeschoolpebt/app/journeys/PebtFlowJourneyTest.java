package org.homeschoolpebt.app.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.homeschoolpebt.app.utils.YesNoAnswer.YES;

import java.time.Duration;
import java.util.Optional;
import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    testPage.clickButton("Add student");
    assertThat(testPage.getTitle()).contains("Do any of the following apply to Stud?");
    testPage.findElementById("none__checkbox").click();
    testPage.clickContinue();
    testPage.clickContinue(); // Which school did <name> unenroll from?
    testPage.clickContinue(); // Which school would <name> attend?
    testPage.clickButton("Yes, this is everyone");

    // Language preference
    testPage.clickContinue();
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
    assertThat(testPage.getHeader()).contains("Is this everyone that lives with you?");
    assertThat(testPage.findElementsByClass("subflow-delete")).hasSize(1);
    // Delete final household member to go back to householdList
    testPage.findElementsByClass("subflow-delete").get(0).click();
    testPage.clickButton("Yes, remove them");
    assertThat(testPage.getTitle()).isEqualTo("Are there other people in the students' household?");
    // Add back household members
    testPage.enter("hasHousehold", YES.getDisplayValue());
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
    testPage.findElementsByClass("subflow-edit").get(0).click();
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

    //click on No I already know....
    testPage.clickLink("No, I already know my annual household pre-tax income - I prefer to enter it directly.");
    assertThat(testPage.getTitle()).isEqualTo("Reported Annual Household Pre-Tax Income");
    testPage.clickContinue();
    assertThat(testPage.hasErrorText("Please enter a value")).isTrue();
    testPage.enter("reportedTotalAnnualHouseholdIncome", "a");
    testPage.clickContinue();
    assertThat(testPage.hasErrorText("Please make sure to enter a valid dollar amount.")).isTrue();

    // Test a high amount to see that we get the exceeds max income page
    testPage.enter("reportedTotalAnnualHouseholdIncome", "300000");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Exceeds Income Threshold");
    testPage.clickButton("Apply anyway");
    assertThat(testPage.getTitle()).isEqualTo("Economic Hardship");
    testPage.goBack();
    testPage.goBack();
    assertThat(testPage.getTitle()).isEqualTo("Reported Annual Household Pre-Tax Income");
    testPage.enter("reportedTotalAnnualHouseholdIncome", "125");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Income Complete");
    testPage.goBack();
    testPage.goBack();
    testPage.goBack();
    testPage.goBack();
    assertThat(testPage.getTitle()).isEqualTo("Income");
  }
}
