package org.homeschoolpebt.app.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.homeschoolpebt.app.utils.YesNoAnswer.NO;
import static org.homeschoolpebt.app.utils.YesNoAnswer.YES;

import org.homeschoolpebt.app.utils.AbstractBasePageTest;
import org.junit.jupiter.api.Test;

public class PebtFlowJourneyTest extends AbstractBasePageTest {

  @Test
  void fullUbiFlow() {
    // Landing screen
    assertThat(testPage.getTitle()).isEqualTo("Apply for UBI payments easily online.");
    testPage.clickButton("Apply now");
    // How this works
    testPage.clickContinue();
    // Language preference
    testPage.clickContinue();
    // Getting to know you
    testPage.clickContinue();

    // Personal info
    testPage.enter("firstName", "Testy");
    testPage.enter("lastName", "McTesterson");

    // test bad date
    testPage.enter("birthDay", "1");
    testPage.enter("birthMonth", "15");
    testPage.enter("birthYear", "2000");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");
    assertThat(testPage.hasErrorText("Make sure to provide a month equal or below 12."));

    // test invalid date
    testPage.enter("birthDay", "abc");
    testPage.enter("birthMonth", "2");
    testPage.enter("birthYear", "2000");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");
    assertThat(testPage.hasErrorText("The date is invalid. Make sure to provide a valid date."));
    assertThat(testPage.hasErrorText("Make sure to provide a day equal or above 01."));
    assertThat(testPage.hasErrorText("Make sure to provide a day equal or below 31."));

    // test single digit day and month
    testPage.enter("birthDay", "1");
    testPage.enter("birthMonth", "2");
    testPage.enter("birthYear", "2000");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Where can the State of California send you mail?");
    testPage.goBack();
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");

    // test two digit dates for day and month
    testPage.enter("birthDay", "11");
    testPage.enter("birthMonth", "12");
    testPage.enter("birthYear", "2000");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Where can the State of California send you mail?");
    testPage.goBack();
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");

    // moved to USA date
    testPage.clickElementById("movedToUSA-No");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Where can the State of California send you mail?");
    testPage.goBack();

    // movedToUSA - check invalid date when movedToUSA=Yes
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");
    testPage.clickElementById("movedToUSA-Yes");
    testPage.enter("movedToUSADay", "65");
    testPage.enter("movedToUSAMonth", "3");
    testPage.enter("movedToUSAYear", "1987");
    testPage.clickContinue();
    assertThat(testPage.hasErrorText("Please check the date entered. It is not a valid date")).isTrue();
    assertThat(testPage.hasErrorText("Make sure to provide a day equal or below 31.")).isFalse();

    // movedToUSA - check correct date when movedToUSA=Yes
    testPage.clickElementById("movedToUSA-Yes");
    testPage.enter("movedToUSADay", "1");
    testPage.enter("movedToUSAMonth", "3");
    testPage.enter("movedToUSAYear", "1987");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Where can the State of California send you mail?");

    testPage.goBack();
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");
    // movedToUSA - try an incorrect date to test leap year out
    testPage.clickElementById("movedToUSA-Yes");
    testPage.enter("movedToUSADay", "29");
    testPage.enter("movedToUSAMonth", "2");
    testPage.enter("movedToUSAYear", "2023");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Personal Info");

    // movedToUSA - try a good leap year out
    testPage.clickElementById("movedToUSA-Yes");
    testPage.enter("movedToUSADay", "29");
    testPage.enter("movedToUSAMonth", "2");
    testPage.enter("movedToUSAYear", "2024");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Where can the State of California send you mail?");

    // Home address
    testPage.enter("residentialAddressStreetAddress1", "1111 N State St");
    testPage.enter("residentialAddressStreetAddress2", "Apt 2");
    testPage.enter("residentialAddressCity", "Roswell");
    testPage.enter("residentialAddressState", "NM - New Mexico");
    testPage.enter("residentialAddressZipCode", "88201");
    testPage.clickContinue();
    // Eligibility
    testPage.clickContinue();
    driver.navigate().to(baseUrl + "/flow/pebt/contactInfo");
    // Contact Info
    assertThat(testPage.getTitle()).isEqualTo("Contact Info");
    testPage.enter("phoneNumber", "(312) 877-1021");
    testPage.enter("email", "foo@test.com");
    // Assert JavaScript is checking the phone and email checkboxes when values are entered
    assertThat(testPage.findElementById("howToContactYou-phoneNumber").isSelected()).isTrue();
    assertThat(testPage.findElementById("howToContactYou-email").isSelected()).isTrue();
    testPage.clickContinue();
    // Eligibility
    testPage.clickContinue();
    // Housemates
    assertThat(testPage.getTitle()).isEqualTo("Housemates");
    testPage.enter("hasHousehold", NO.getDisplayValue());
    // Income screen
    assertThat(testPage.getTitle()).isEqualTo("Income");
    // Go back to household page and select yes instead
    testPage.goBack();
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
    assertThat(testPage.getTitle()).isEqualTo("Housemates");
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
    //click on No I already know....
    assertThat(testPage.getTitle()).isEqualTo("Income");
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