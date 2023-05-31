package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

  public static String getStandardOperatingExpensesAmount(Map<String, Object> fieldData) {
    Object rawGrossAmount = fieldData.get("incomeGrossMonthlyIndividual");
    if (rawGrossAmount == null) {
      // TODO: Null handling? Redirect?
      return "";
    }
    Float grossAmount = Float.parseFloat(rawGrossAmount.toString());
    return decimalFormatWithoutComma.format(grossAmount * 0.4);
  }

  public static String getSelfEmployedOperatingExpensesAmount(Map<String, Object> fieldData) {
    boolean useCustomOperatingExpenses = (
        fieldData.get("incomeSelfEmployedCustomOperatingExpenses") != null &&
            fieldData.get("incomeSelfEmployedCustomOperatingExpenses").equals("true") &&
            fieldData.get("incomeSelfEmployedOperatingExpenses") != null
    );
    double expenses;
    if (useCustomOperatingExpenses) {
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

  public static String getSelfEmployedNetIncomeAmount(Map<String, Object> fieldData) {
    Object rawGrossAmount = fieldData.get("incomeGrossMonthlyIndividual");
    if (rawGrossAmount == null) {
      // TODO: Null handling? Redirect?
      return "";
    }
    double grossMonthly = Double.parseDouble(rawGrossAmount.toString());
    boolean useCustomOperatingExpenses = (
        fieldData.get("incomeSelfEmployedCustomOperatingExpenses") != null &&
            fieldData.get("incomeSelfEmployedCustomOperatingExpenses").equals("true") &&
            fieldData.get("incomeSelfEmployedOperatingExpenses") != null
    );

    double netMonthly;
    if (useCustomOperatingExpenses) {
      netMonthly = grossMonthly - Double.parseDouble(fieldData.get("incomeSelfEmployedOperatingExpenses").toString());
    } else {
      netMonthly = 0.6 * grossMonthly;
    }
    return formatMoney(String.valueOf(netMonthly * 12));
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


    DecimalFormat decimalFormat = new DecimalFormat("###.##");
    String formattedValue = "$" + decimalFormat.format(numericVal);
    return formattedValue;
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

    if (street1.isBlank()) {
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

