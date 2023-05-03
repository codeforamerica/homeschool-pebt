package org.homeschoolpebt.app.inputs;

import formflow.library.data.FlowInputs;
import formflow.library.data.validators.Money;
import formflow.library.data.validators.Phone;
import formflow.library.utils.RegexUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Pebt extends FlowInputs {

  private MultipartFile ubiFiles;

  // Pre-screen
  private String hasMoreThanOneStudent;
  private String isApplyingForSelf;
  private String isEnrolledInVirtualSchool;
  private String hasUnenrolled;
  @NotBlank
  private String unenrolledSchoolName;

  // Students subflow
  private String studentFirstName;
  private String studentMiddleInitial;
  private String studentLastName;
  private String studentSchoolType; // TODO: Make this an enum.
  private ArrayList<String> studentDesignations; // TODO: Add validation in case the Javascript fails?
  @NotBlank
  private String studentUnenrolledSchoolName;
  @NotBlank
  private String studentWouldAttendSchoolName;
  private String applicantIsInHousehold;

  // Language Preferences Screen
  private String languageRead;
  private String languageSpoken;
  private String needInterpreter;

  // Personal Info Screen
  @NotBlank(message = "{personal-info.provide-first-name}")
  private String firstName;
  @NotBlank(message = "{personal-info.provide-last-name}")
  private String lastName;

  // Home Address Screen
  @NotBlank
  private String residentialAddressStreetAddress1;
  private String residentialAddressStreetAddress2;
  @NotBlank
  private String residentialAddressCity;
  @NotBlank
  private String residentialAddressState;
  @NotBlank
  private String residentialAddressZipCode;

  // Verify Home Address Screen
  private String useValidatedResidentialAddress;

  // Housemates Screen
  private String hasHousehold;

  // Housemate Info Screen
  @NotBlank
  private String householdMemberFirstName;
  @NotBlank
  private String householdMemberLastName;
  private String householdMemberReceivesBenefits;
  private String householdMemberBenefitsCaseNumber;
  private String householdMemberBenefitsCaseNumberFDPIR;
  private String householdAddressFile;  // file id

  // Household Member Income Screen
  @NotBlank(message = "{household-member-income.failed-to-make-selection}")
  private String incomeMember;

  // Income Types Screen
  private String hasIncome;
  private String incomeJobsCount;
  private String incomeWasSelfEmployed;
  private String incomeGrossMonthlyIndividual;

  @NotEmpty(message = "{income-types.error}")
  private ArrayList<String> incomeTypes;

  // Income Amounts Screen
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeJobAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSelfAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeUnemploymentAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSocialSecurityAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeRetirementAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeChildOrSpousalSupportAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomePensionAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeInvestmentAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeCapitalGainsAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeRentalOrRoyaltyAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeFarmOrFishAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeAlimonyAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeTaxableScholarshipAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeCancelledDebtAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeCourtAwardsAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeGamblingAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeJuryDutyPayAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeOtherAmount;

  // Reported Household Annual Income Screen
  @NotBlank(message = "{household-reported-annual-pre-tax-income.please-enter-a-value}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String reportedTotalAnnualHouseholdIncome;

  //Economic Hardship Screen
  private ArrayList<String> economicHardshipTypes;

  @NotEmpty(message = "{legal-stuff.make-sure-you-answer-this-question}")
  private ArrayList<String> agreesToLegalTerms;
  @NotBlank
  private String signature;
  @Phone(message = "{contact-info.invalid-phone-number}")
  private String phoneNumber;
  @Email(message = "{contact-info.invalid-email}", regexp = RegexUtils.EMAIL_REGEX)
  private String email;
  @NotEmpty
  private ArrayList<String> howToContactYou;


}
