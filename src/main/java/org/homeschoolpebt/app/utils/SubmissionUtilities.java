package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.inputs.Pebt;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

  public static String getLastMonth(Submission submission) {
    var day = new DateTime().minusMonths(1).withDayOfMonth(1);
    DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM, yyyy");
    return day.toString(formatter);
  }

  // "householdList" -> "household-list"
  public static String transifexKeyPrefix(String requestUri) {
    return requestUri.replaceAll("([A-Z])", "-$1").toLowerCase();
  }

  public static Integer getHouseholdMemberCount(Submission submission) {
    var students = (List<Object>) submission.getInputData().getOrDefault("students", new ArrayList<Object>());
    var householdMembers = (List<Object>) submission.getInputData().getOrDefault("household", new ArrayList<Object>());

    return 1 + students.size() + householdMembers.size();
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

  public static Boolean useSelfEmploymentCustomExpenses(Pebt.Income job) {
    return job.getIncomeSelfEmployedCustomOperatingExpenses() != null &&
      job.getIncomeSelfEmployedCustomOperatingExpenses().equals("true") &&
      job.getIncomeSelfEmployedOperatingExpenses() != null;
  }

  public static String getSelfEmployedOperatingExpensesAmount(Map<String, Object> fieldData) {
    var job = new Pebt.Income();
    job.populate(fieldData);

    double expenses;
    if (useSelfEmploymentCustomExpenses(job)) {
      expenses = Double.parseDouble(job.getIncomeSelfEmployedOperatingExpenses());
    } else {
      Object rawGrossAmount = job.getIncomeGrossMonthlyIndividual();
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
    var job = new Pebt.Income();
    job.populate(fieldData);
    return formatMoney(getSelfEmployedNetIncomeAmount(job, TimePeriod.YEARLY));
  }

  public static Double getSelfEmployedNetIncomeAmount(Pebt.Income job, TimePeriod period) {
    Object rawGrossAmount = job.getIncomeGrossMonthlyIndividual();
    if (rawGrossAmount == null) {
      // TODO: Null handling? Redirect?
      return null;
    }
    double grossMonthly = Double.parseDouble(rawGrossAmount.toString());
    double netMonthly;
    if (useSelfEmploymentCustomExpenses(job)) {
      netMonthly = grossMonthly - Double.parseDouble(job.getIncomeSelfEmployedOperatingExpenses());
    } else {
      netMonthly = 0.6 * grossMonthly;
    }
    if (period == TimePeriod.MONTHLY) {
      return netMonthly;
    } else {
      return netMonthly * 12;
    }
  }

  public static Double getHourlyGrossIncomeAmount(Pebt.Income job) {
    var hours = Double.parseDouble(job.getIncomeHoursPerWeek());
    var wage = Double.parseDouble(job.getIncomeHourlyWage());

    return hours * wage;
  }

  public static String getHourlyGrossIncomeExplanation(Pebt.Income job) {
    var hours = Double.parseDouble(job.getIncomeHoursPerWeek());
    var wage = Double.parseDouble(job.getIncomeHourlyWage());

    return "%s hours * %s per hour".formatted(hours, formatMoney(wage));
  }

  public static Double getRegularPayAmount(Pebt.Income job) {
    var amount = Double.parseDouble(job.getIncomeRegularPayAmount());
    switch (job.getIncomeRegularPayInterval()) {
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

  public static String getRegularPayExplanation(Pebt.Income job) {
    var amount = Double.parseDouble(job.getIncomeRegularPayAmount());
    switch (job.getIncomeRegularPayInterval()) {
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
}

