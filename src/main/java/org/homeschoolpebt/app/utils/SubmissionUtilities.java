package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.preparers.StudentsPreparer;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SubmissionUtilities {

  public static DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
  public static DecimalFormat decimalFormatWithoutComma = new DecimalFormat("#.00");
  public static final String APPLICANT = "applicant";
  public static final String REPORTED_TOTAL_ANNUAL_HOUSEHOLD_INCOME = "reportedTotalAnnualHouseholdIncome";
  public static final String HOUSEHOLD_MEMBER = "householdMember";
  public static final String HOUSEHOLD = "household";
  public static final String INCOME = "income";
  public static final String ITERATION_UUID = "uuid";

  /**
   * This function returns a String of the formatted submitted_at date.   The method returns a date that looks like this: "February 7, 2023".
   *
   * @param submission submssion contains the submittedAt instance variable that holds the date the application was submitted.
   * @return a string containing the formatted date.
   */
  public static String getFormattedSubmittedAtDate(Submission submission) {
    String pattern = "MMMM d, yyyy";
    SimpleDateFormat formatDate = new SimpleDateFormat(pattern);
    return formatDate.format(submission.getSubmittedAt());
  }

  // Removes leading zeroes, i.e. "001012344" -> "1012344"
  public static String getFormattedConfirmationNumber(String confirmationNumber) {
    return confirmationNumber.replaceFirst("^0*", "");
  }

  public static String getLastMonth(Submission submission) {
    var day = new DateTime().minusMonths(1).withDayOfMonth(1);
    DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM, yyyy");
    return day.toString(formatter);
  }

  // "householdList" -> "household-list"
  public static String transifexKeyPrefix(String requestUri) {
    return requestUri.replaceAll("([A-Z])", "-$1").toLowerCase();
  }

  public static String householdMemberFullName(Map<String, String> householdMember) {
    return householdMember.get("householdMemberFirstName") + " " + householdMember.get("householdMemberLastName");
  }

  public static String applicantFullName(Submission submission) {
    var inputData = submission.getInputData();
    return (String)inputData.getOrDefault("firstName", "") + ' ' + (String)inputData.getOrDefault("lastName", "");
  }

  public static String studentFullName(Map<String, String> student) {
    if (((String)student.getOrDefault("studentMiddleInitial", "")).isBlank()) {
      return student.get("studentFirstName") + " " + student.get("studentLastName");
    } else {
      return student.get("studentFirstName") + " " + student.get("studentMiddleInitial") + " " + student.get("studentLastName");
    }
  }

  public static List<String> getHouseholdMemberNames(Submission submission) {
    ArrayList<String> names = new ArrayList<>();

    var applicantName = submission.getInputData().get("firstName") + " " + submission.getInputData().get("lastName");
    var students = (List<Map<String, String>>) submission.getInputData().getOrDefault("students", new ArrayList<HashMap<String, Object>>());
    var householdMembers = (List<Map<String, String>>) submission.getInputData().getOrDefault("household", new ArrayList<HashMap<String, Object>>());

    names.add(applicantName);
    students.forEach(s -> names.add(studentFullName(s)));
    householdMembers.forEach(hh -> names.add(householdMemberFullName(hh)));

    return names;
  }

  public static Integer getHouseholdMemberCount(Submission submission) {
    return getHouseholdMemberNames(submission).size();
  }

  public static String getStandardOperatingExpensesAmount(Map<String, Object> fieldData) {
    Object rawGrossAmount = fieldData.get("incomeGrossMonthlyIndividual");
    if (rawGrossAmount == null) {
      // TODO: Null handling? Redirect?
      return "";
    }
    Float grossAmount = Float.parseFloat(rawGrossAmount.toString());
    return decimalFormatWithoutComma.format(grossAmount * 0.4);
  }

  public static Boolean useSelfEmploymentCustomExpenses(Map<String, Object> fieldData) {
    return fieldData.get("incomeSelfEmployedCustomOperatingExpenses") != null &&
      fieldData.get("incomeSelfEmployedCustomOperatingExpenses").equals("true") &&
      fieldData.get("incomeSelfEmployedOperatingExpenses") != null;
  }

  public static String getSelfEmployedOperatingExpensesAmount(Map<String, Object> fieldData) {
    double expenses;
    if (useSelfEmploymentCustomExpenses(fieldData)) {
      expenses = Double.parseDouble(fieldData.get("incomeSelfEmployedOperatingExpenses").toString());
    } else {
      Object rawGrossAmount = fieldData.get("incomeGrossMonthlyIndividual");
      if (rawGrossAmount == null) {
        // TODO: Null handling? Redirect?
        expenses = 0;
      } else {
        double grossMonthly = Double.parseDouble(rawGrossAmount.toString());
        expenses = 0.4 * grossMonthly;
      }
    }
    return formatMoney(String.valueOf(expenses));
  }

  public enum TimePeriod { MONTHLY, YEARLY };

  public static String getSelfEmployedNetIncomeAmountYearly(Map<String, Object> fieldData) {
    return formatMoney(getSelfEmployedNetIncomeAmount(fieldData, TimePeriod.YEARLY));
  }

  public static Double getSelfEmployedNetIncomeAmount(Map<String, Object> fieldData, TimePeriod period) {
    Object rawGrossAmount = fieldData.get("incomeGrossMonthlyIndividual");
    if (rawGrossAmount == null) {
      // TODO: Null handling? Redirect?
      return null;
    }
    double grossMonthly = Double.parseDouble(rawGrossAmount.toString());
    double netMonthly;
    if (useSelfEmploymentCustomExpenses(fieldData)) {
      netMonthly = grossMonthly - Double.parseDouble(fieldData.get("incomeSelfEmployedOperatingExpenses").toString());
    } else {
      netMonthly = 0.6 * grossMonthly;
    }
    if (period == TimePeriod.MONTHLY) {
      return netMonthly;
    } else {
      return netMonthly * 12;
    }
  }

  public static Double getHourlyGrossIncomeAmount(Map<String, Object> fieldData) {
    var hours = Double.parseDouble(fieldData.get("incomeHoursPerWeek").toString());
    var wage = Double.parseDouble(fieldData.get("incomeHourlyWage").toString());

    return hours * wage * 4;
  }

  public static String getHourlyGrossIncomeExplanation(Map<String, Object> fieldData) {
    var hours = Double.parseDouble(fieldData.get("incomeHoursPerWeek").toString());
    var wage = Double.parseDouble(fieldData.get("incomeHourlyWage").toString());

    return "%s hrs/wk * %s/hr * 4 weeks".formatted(hours, formatMoney(wage));
  }

  public static String getRegularPayAmountFormatted(Map<String, Object> fieldData) {
    return formatMoney(getRegularPayAmount(fieldData));
  }

  public static Double getRegularPayAmount(Map<String, Object> fieldData) {
    var amount = Double.parseDouble(fieldData.get("incomeRegularPayAmount").toString());
    switch (fieldData.getOrDefault("incomeRegularPayInterval", "").toString()) {
      case "weekly" -> {
        // These multipliers are copied from the USDA Prototype Application form.
        return amount * 52 / 12;
      }
      case "biweekly" -> {
        return amount * 26 / 12;
      }
      case "semimonthly" -> {
        return amount * 24 / 12;
      }
      case "monthly" -> {
        return amount;
      }
      case "seasonally", "yearly" -> {
        return amount / 12;
      }
      default -> {
        return null;
      }
    }
  }

  public static String getRegularPayExplanation(Map<String, Object> fieldData) {
    var amount = Double.parseDouble(fieldData.get("incomeRegularPayAmount").toString());
    switch (fieldData.getOrDefault("incomeRegularPayInterval", "").toString()) {
      case "weekly" -> {
        return "%s every week".formatted(formatMoney(amount));
      }
      case "biweekly" -> {
        return "%s every 2 weeks".formatted(formatMoney(amount));
      }
      case "semimonthly" -> {
        return "%s twice a month".formatted(formatMoney(amount));
      }
      case "monthly" -> {
        return "%s monthly".formatted(formatMoney(amount));
      }
      case "seasonally" -> {
        return "%s seasonally".formatted(formatMoney(amount));
      }
      case "yearly" -> {
        return "%s yearly".formatted(formatMoney(amount));
      }
      default -> {
        return null;
      }
    }
  }

  public static String formatMoney(String value) {
    if (value == null) {
      return "";
    }

    double numericVal;
    try {
      numericVal = Double.parseDouble(value);
    } catch (NumberFormatException _e) {
      return value;
    }

    return formatMoney(numericVal);
  }

  public static String formatMoney(Double value) {
    DecimalFormat decimalFormat = new DecimalFormat("###.##");
    return "$" + decimalFormat.format(value);
  }

  /**
   * Return the combined mailing address.
   *
   * @param submission submssion contains the submittedAt instance variable that holds the
   *                   date the application was submitted.
   * @return a string containing the address.
   */
  public static String combinedAddress(Submission submission) {
    String street1;
    String street2;
    String city;
    String state;

    if (submission.getInputData().get("useValidatedResidentialAddress") == "true") {
      street1 = (String) submission.getInputData().get("residentialAddressStreetAddress1_validated");
      street2 = (String) submission.getInputData().getOrDefault("residentialAddressStreetAddress2_validated", "");
      city = (String) submission.getInputData().get("residentialAddressCity_validated");
      state = (String) submission.getInputData().get("residentialAddressState_validated");
    } else {
      street1 = (String) submission.getInputData().get("residentialAddressStreetAddress1");
      street2 = (String) submission.getInputData().getOrDefault("residentialAddressStreetAddress2", "");
      city = (String) submission.getInputData().get("residentialAddressCity");
      state = (String) submission.getInputData().get("residentialAddressState");
    }

    if (street1 == null || street1.isBlank()) {
      return "";
    } else if (!street2.isBlank()) {
      return street1 + ", " + street2 + ", " + city + ", " + state;
    } else {
      return street1 + ", " + city + ", " + state;
    }
  }

  public static String zipCode(Submission submission) {
    if (submission.getInputData().get("useValidatedResidentialAddress") == "true") {
      return (String) submission.getInputData().get("residentialAddressZipCode_validated");
    } else {
      return (String) submission.getInputData().get("residentialAddressZipCode");
    }
  }


  public static ArrayList<HashMap<String, Object>> getHouseholdIncomeReviewItems(Submission submission) {
    var applicantFullName = submission.getInputData().getOrDefault("firstName", "") + " " + submission.getInputData().getOrDefault("lastName", "");
    var notYetShownNames = getHouseholdMemberNames(submission);
    ArrayList<HashMap<String, Object>> items = new ArrayList<>();

    for (var job : (List<HashMap<String, Object>>) submission.getInputData().getOrDefault("income", new ArrayList<HashMap<String, Object>>())) {
      var item = new HashMap<String, Object>();
      item.put("name", job.get("incomeMember"));
      item.put("itemType", "job");
      item.put("jobName", job.get("incomeJobName"));
      item.put("isApplicant", job.get("incomeMember").equals(applicantFullName));
      item.put("income", formatMoney(IncomeCalculator.futureIncomeForJob(job)));
      item.put("uuid", job.get("uuid"));

      if (job.getOrDefault("incomeWillBeLess", "false").toString().equals("true")) {
        if (job.getOrDefault("incomeSelfEmployed", "false").toString().equals("true")) {
          item.put("incomeType", "net-pay-estimate");
        } else {
          item.put("incomeType", "gross-pay-estimate");
        }
      } else {
        if (job.getOrDefault("incomeSelfEmployed", "false").toString().equals("true")) {
          item.put("incomeType", "net-pay");
        } else {
          item.put("incomeType", "gross-pay");
        }
      }

      notYetShownNames.remove(job.get("incomeMember"));
      items.add(item);
    }

    // Add any household members that didn't have income entries
    notYetShownNames.forEach(name -> {
      var item = new HashMap<String, Object>();
      item.put("name", name);
      item.put("itemType", "no-jobs-added");
      item.put("isApplicant", name.equals(applicantFullName));

      items.add(item);
    });

    // Sort the list so the applicant shows up first and the rest of the names are alphabetical
    items.sort(Comparator.comparing(
      job -> (String)job.get("name"),
      (a, b) -> {
        if (a.equals(applicantFullName) && !b.equals(applicantFullName)) {
          return -1;
        } else if (b.equals(applicantFullName) && !a.equals(applicantFullName)) {
          return 1;
        } else {
          return a.compareTo(b);
        }
      }));

    // Set combineWithPrevious on items after the first one for the same person
    for (var i = 0; i < items.size(); i++) {
      var item = items.get(i);
      var combineWithPrevious = (i > 0) && items.get(i - 1).get("name").equals(items.get(i).get("name"));
      items.get(i).put("combineWithPrevious", combineWithPrevious);
    }

    items.add(new HashMap<String, Object>() {{
      put("name", null);
      put("itemType", "household-total");
      put("income", formatMoney(new IncomeCalculator(submission).totalFutureEarnedIncome()));
    }});

    return items;
  }

  public static ArrayList<HashMap<String, String>> getDocUploadIdentityStudentsList(Submission submission) {
    var items = new ArrayList<HashMap<String, String>>();
    if (submission.getInputData().getOrDefault("isApplyingForSelf", "false").equals("true")) {
      var item = new HashMap<String, String>();
      item.put("name", applicantFullName(submission));
      item.put("isApplicant", "true");
      items.add(item);
    }

    var students = (List<Map<String, String>>) submission.getInputData().getOrDefault("students", new ArrayList<HashMap<String, Object>>());
    for (var student : students) {
      var item = new HashMap<String, String>();
      item.put("name", studentFullName(student));
      item.put("isApplicant", "false");
      items.add(item);
    }

    return items;
  }

  public static ArrayList<HashMap<String, String>> getDocUploadEnrollmentStudentsList(Submission submission) {
    var items = new ArrayList<HashMap<String, String>>();
    if (submission.getInputData().getOrDefault("isApplyingForSelf", "false").equals("true")) {
      var item = new HashMap<String, String>();
      item.put("name", applicantFullName(submission));
      item.put("isApplicant", "true");
      items.add(item);
    }

    var students = (List<Map<String, String>>) submission.getInputData().getOrDefault("students", new ArrayList<HashMap<String, Object>>());
    for (var student : students) {
      if (!student.getOrDefault("studentHomeschoolAffidavitNumber", "").isBlank()) {
        // we don't need documentation from students who have a homeschool affidavit number
        continue;
      }

      var item = new HashMap<String, String>();
      item.put("name", studentFullName(student));
      item.put("isApplicant", "false");
      items.add(item);
    }

    return items;
  }

  public static ArrayList<HashMap<String, String>> getDocUploadUnearnedIncomeList(Submission submission) {
    var incomeTypes = new ArrayList<String>();
    incomeTypes.addAll((List<String>) submission.getInputData().getOrDefault("incomeUnearnedRetirementTypes[]", new ArrayList<String>()));
    incomeTypes.addAll((List<String>) submission.getInputData().getOrDefault("incomeUnearnedTypes[]", new ArrayList<String>()));
    var items = new ArrayList<HashMap<String, String>>();

    for (var incomeType : incomeTypes) {
      if (incomeType.equals("none")) {
        continue;
      }

      var item = new HashMap<String, String>();
      var amount = Double.parseDouble((String)submission.getInputData().getOrDefault(incomeType + "Amount", "0"));
      item.put("type", incomeType);
      item.put("amount", formatMoney(amount));
      items.add(item);
    }

    return items;
  }

  public static boolean allStudentsEligible(Submission submission) {
    var students = (List<Map<String, String>>) submission.getInputData().getOrDefault("students", new ArrayList<HashMap<String, Object>>());

    return students.stream()
      .map(student -> student.getOrDefault("studentUnenrolledSchoolName", ""))
      .allMatch(schoolName -> StudentsPreparer.OFFICAL_SCHOOL_FORMAT.matcher(schoolName).matches());
  }

  public static boolean needsIncomeVerification(Submission submission) {
    var students = (List<Map<String, Object>>) submission.getInputData().getOrDefault("students", new ArrayList<HashMap<String, Object>>());

    // All students have designation (foster/runaway/etc.)?
    var allStudentsHaveDesignation = students.stream().allMatch(student -> {
      var designations = (List<String>) student.getOrDefault("studentDesignations[]", new ArrayList<String>());
      return designations.size() >= 1 && !designations.get(0).equals("none");
    });
    if (allStudentsHaveDesignation) {
      return false;
    }

    // All students would have attended a CEP school?
    var wouldAttendCdsCodes = students.stream().map(student -> {
      var wouldAttendSchoolName = (String) student.getOrDefault("studentWouldAttendSchoolName", "");
      var parsed = StudentsPreparer.parseSchoolName(wouldAttendSchoolName);
      return parsed.get(0);
    }).toList();
    var allStudentsWouldAttendCepSchool = SchoolListUtilities.allCepSchools((List<String>) wouldAttendCdsCodes);
    if (allStudentsWouldAttendCepSchool) {
      return false;
    }

    // Any household member receiving benefits?
    var receivingBenefits = submission.getInputData().getOrDefault("householdMemberReceivesBenefits", "none");
    if (!receivingBenefits.equals("None of the Above")) {
      return false;
    }

    return true;
  }
  static final DateTime LAST_DAY_OF_APPLICATIONS = new DateTime(2023, 8, 15, 17, 00); // 5pm on Aug 15
  public static String getLaterdocDeadline(Date now) {
    var oneWeekHence = new DateTime(now).plus(Duration.standardDays(7));
    var deadline = oneWeekHence.compareTo(LAST_DAY_OF_APPLICATIONS) < 0 ? oneWeekHence : LAST_DAY_OF_APPLICATIONS;

    String pattern = "MMMM d, yyyy";
    SimpleDateFormat formatDate = new SimpleDateFormat(pattern);
    return formatDate.format(deadline.toDate());
  }

  public static List<String> getMissingDocUploads(Submission submission) {
    var missing = new ArrayList<String>();

    var skippedIdentity = submission.getInputData().getOrDefault("identityFiles", "[]").equals("[]");
    if (skippedIdentity) {
      missing.add("identity");
    }

    var neededEnrollment = needsEnrollmentDocs(submission);
    var skippedEnrollment = submission.getInputData().getOrDefault("enrollmentFiles", "[]").equals("[]");
    if (neededEnrollment && skippedEnrollment) {
      missing.add("enrollment");
    }

    var neededIncome = needsIncomeVerification(submission);
    var skippedIncome = submission.getInputData().getOrDefault("incomeFiles", "[]").equals("[]");
    var neededUnearned = getDocUploadUnearnedIncomeList(submission).size() > 0;
    var skippedUnearned = submission.getInputData().getOrDefault("unearnedIncomeFiles", "[]").equals("[]");
    if ((neededIncome && skippedIncome) || (neededUnearned && skippedUnearned)) {
      missing.add("income");
    }

    return missing;
  }

  public static boolean needsEnrollmentDocs(Submission submission) {
    return getDocUploadEnrollmentStudentsList(submission).size() > 0;
  }

  public static String getSubmissionLanguage(Submission submission) {
    return submission.getUrlParams() == null ? "en" : submission.getUrlParams().getOrDefault("lang", "en");
  }

  public static boolean isApplyingForSelf(Submission submission) {
    return submission.getInputData().getOrDefault("isApplyingForSelf", "false").equals("true");
  }

  public static boolean applicantIsInHousehold(Submission submission) {
    return submission.getInputData().getOrDefault("applicantIsInHousehold", "false").equals("true");
  }
}

