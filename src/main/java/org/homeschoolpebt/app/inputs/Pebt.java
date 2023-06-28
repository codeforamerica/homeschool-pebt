package org.homeschoolpebt.app.inputs;

import formflow.library.data.FlowInputs;
import formflow.library.data.validators.Money;
import formflow.library.data.validators.Phone;
import formflow.library.utils.RegexUtils;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = true)
public class Pebt extends FlowInputs {

  private MultipartFile identityFiles;
  private MultipartFile enrollmentFiles;
  private MultipartFile incomeFiles;
  private MultipartFile unearnedIncomeFiles;

  // Pre-screen
  private String hasMoreThanOneStudent;
  private String isApplyingForSelf;
  private String isEnrolledInVirtualSchool;
  private String hasUnenrolled;
  @NotBlank(message = "{pre-screen-unenrolled-school-name.please-enter-a-value}")
  private String unenrolledSchoolName;

  // Students subflow
  @NotBlank
  private String studentFirstName;
  private String studentMiddleInitial;
  private String studentLastName;
  @NotBlank
  private String studentSchoolType; // TODO: Make this an enum.
  @NotBlank(message = "{date-error.day-presence}")
  @Min(value = 1, message = "{date-error.day-range}")
  @Max(value = 31, message = "{date-error.day-range}")
  private String studentBirthdayDay;
  @NotBlank(message = "{date-error.month-presence}")
  @Min(value = 1, message = "{date-error.month-range}")
  @Max(value = 12, message = "{date-error.month-range}")
  private String studentBirthdayMonth;
  @NotBlank(message = "{date-error.year-presence}")
  @Min(value = 1850, message = "{date-error.year-range}")
  @Max(value = 2100, message = "{date-error.year-range}")
  private String studentBirthdayYear;
  private String studentBirthdayDate;
  private ArrayList<String> studentDesignations; // TODO: Add validation in case the Javascript fails?
  @NotBlank
  private String studentGrade;
  private String studentHomeschoolAffidavitNumber; // TODO: Validate this is present if studentSchoolType = homeschool
  private String studentVirtualSchoolName; // TODO: Validate this is present if studentSchoolType = virtual
  @NotBlank
  private String studentUnenrolledSchoolName;
  @NotBlank
  private String studentWouldAttendSchoolName;
  private String applicantIsInHousehold;

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
  @Size(min=1, max=15, message="{household-receives-benefits.calfresh-provide-1-15-digits}")
  private String householdMemberBenefitsCaseNumberCalfresh;
  @Size(min=1, max=15, message="{household-receives-benefits.calworks-provide-1-15-digits}")
  private String householdMemberBenefitsCaseNumberCalworks;
  private String householdMemberBenefitsCaseNumberFDPIR;
  private String householdAddressFile;  // file id

  // Household Member Income Screen
  @NotBlank(message = "{household-member-income.failed-to-make-selection}")
  private String incomeMember;

  // Job name screen
  @NotBlank
  private String incomeJobName;

  // Income Types Screen
  private String hasIncome;
  private String incomeJobsCount;
  private String incomeSelfEmployed;
  private String incomeSelfEmployedCustomOperatingExpenses;
  private String incomeTransientStandardOperatingExpensesAmount; // Transient field for passing data into the self-employed expenses validator


  // TODO: Validate this is > 40% of incomeGrossMonthlyIndividual
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSelfEmployedOperatingExpenses;
  private String incomeGrossMonthlyIndividual;
  private String incomeIsJobHourly;
  // TODO: Validate this is a number
  private String incomeHourlyWage;
  // TODO: Validate this is a number
  private String incomeHoursPerWeek;

  @NotBlank
  private String incomeRegularPayInterval;
  @NotBlank
  @Money
  private String incomeRegularPayAmount;
  private String incomeWillBeLess;
  @NotBlank
  @Money
  private String incomeCustomAnnualIncome;
  @NotBlank
  @Money
  private String incomeCustomMonthlyIncome;
  private String incomeWillBeLessDescription;

  @NotEmpty(message = "{income-unearned-types.error}")
  private ArrayList<String> incomeUnearnedRetirementTypes;

  @NotEmpty(message = "{income-unearned-types.error}")
  private ArrayList<String> incomeUnearnedTypes;

  // Unearned income amounts screen - retirement section
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSocialSecurityAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSSIAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String income401k403bAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomePensionAmount;

  // Unearned income amounts screen - general section
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeUnemploymentAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeWorkersCompensationAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSpousalSupportAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeChildSupportAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeDisabilityAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeVeteransAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeOtherAmount;
  private String incomeUnearnedDescription;

  // Reported Household Annual Income Screen
  @NotBlank(message = "{household-reported-annual-pre-tax-income.please-enter-a-value}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String reportedTotalAnnualHouseholdIncome;

  @NotEmpty(message = "{legal-stuff.make-sure-you-answer-this-question}")
  private ArrayList<String> agreesToLegalTerms;
  @NotBlank
  private String signature;
  @NotBlank(message = "{contact-info.missing-phone-number}")
  @Phone(message = "{contact-info.invalid-phone-number}")
  private String phoneNumber;
  @Email(message = "{contact-info.invalid-email}", regexp = RegexUtils.EMAIL_REGEX)
  private String email;
}
