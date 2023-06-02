package org.homeschoolpebt.app.inputs;

import formflow.library.data.FlowInputs;
import formflow.library.data.Submission;
import formflow.library.data.validators.Money;
import formflow.library.data.validators.Phone;
import formflow.library.utils.RegexUtils;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

@Data
public class Pebt extends FlowInputs {

  private MultipartFile ubiFiles;

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
  private List<String> studentDesignations; // TODO: Add validation in case the Javascript fails?
  @NotBlank
  private String studentGrade;
  private String studentHomeschoolAffidavitNumber; // TODO: Validate this is present if studentSchoolType = homeschool
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
  private String householdMemberBenefitsCaseNumber;
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

  // TODO: Validate this is > 40% of incomeGrossMonthlyIndividual
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSelfEmployedOperatingExpenses;
  private String incomeGrossMonthlyIndividual;
  private String incomeIsJobHourly;
  // TODO: Validate this is a number
  private String incomeHourlyWage;
  // TODO: Validate this is a number
  private String incomeHoursPerWeek;
  // TODO: Validate this is a number
  private String incomeRegularPayInterval;
  // TODO: Validate this is a number
  private String incomeRegularPayAmount;
  private String incomeWillBeLess;
  private String incomeCustomAnnualIncome;
  private String incomeWillBeLessDescription;
  private String incomeCalculationMethod;

  public enum INCOME_TYPES { incomeUnemployment, incomeWorkersCompensation, incomeSpousalSupport, incomeChildSupport, incomePension, incomeRetirement, incomeSSI, incomeOther };
  @NotEmpty(message = "{income-unearned-types.error}")
  @Enumerated(EnumType.STRING)
  private List<INCOME_TYPES> incomeTypes;

  // Income Amounts Screen
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
  private String incomePensionAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeRetirementAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeSSIAmount;
  @NotBlank(message = "{income-amounts.must-select-one}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String incomeOtherAmount;

  // Reported Household Annual Income Screen
  @NotBlank(message = "{household-reported-annual-pre-tax-income.please-enter-a-value}")
  @Money(message = "{income-amounts.must-be-dollars-cents}")
  private String reportedTotalAnnualHouseholdIncome;

  //Economic Hardship Screen
  private List<String> economicHardshipTypes;

  @NotEmpty(message = "{legal-stuff.make-sure-you-answer-this-question}")
  private List<String> agreesToLegalTerms;
  @NotBlank
  private String signature;
  @Phone(message = "{contact-info.invalid-phone-number}")
  private String phoneNumber;
  @Email(message = "{contact-info.invalid-email}", regexp = RegexUtils.EMAIL_REGEX)
  private String email;
  @NotEmpty
  private List<String> howToContactYou;

  public static Pebt fromSubmission(Submission submission) {
    Pebt newInstance = new Pebt();

    var inputData = new HashMap<>(submission.getInputData());
    // Replace "anyField[]" keys with "anyField"
    inputData.keySet().stream().filter(key -> key.endsWith("[]")).toList().forEach(key -> {
      var value = inputData.remove(key);
      inputData.put(key.substring(0, key.length() - 2), value);
    });

    try {
      BeanUtils.populate(newInstance, inputData);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }

    return newInstance;
  }

  // NOTE: We would have to keep these fields in sync with the top-level object.
  public List<Student> students;
  @Data
  public class Student {
    @NotBlank
    private String studentFirstName;
    private String studentMiddleInitial;
    private String studentLastName;
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
    private List<String> studentDesignations; // TODO: Add validation in case the Javascript fails?
    @NotBlank
    private String studentGrade;
    private String studentHomeschoolAffidavitNumber; // TODO: Validate this is present if studentSchoolType = homeschool
    private String studentVirtualSchoolName; // TODO: Validate this is present if studentSchoolType = virtual
    @NotBlank
    private String studentUnenrolledSchoolName;
    @NotBlank
    private String studentWouldAttendSchoolName;
    private String applicantIsInHousehold;
  }
}
