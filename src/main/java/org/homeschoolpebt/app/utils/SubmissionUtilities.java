package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

  public static List<Map<String, Object>> sortIncomeNamesWithApplicantFirst(Submission submission) {
    Map<String, Object> inputData = submission.getInputData();
    if (inputData.containsKey(INCOME)) {
      ArrayList<Map<String, Object>> subflow =
          (ArrayList<Map<String, Object>>) submission.getInputData().get(INCOME);
      Stream<Map<String, Object>> applicantEntry = subflow.stream()
          .filter(entry -> entry.get(HOUSEHOLD_MEMBER).equals(APPLICANT));
      Stream<Map<String, Object>> nonApplicantEntries = subflow.stream()
          .filter(entry -> !entry.get(HOUSEHOLD_MEMBER).equals(APPLICANT));
      return Stream.concat(applicantEntry, nonApplicantEntries).toList();
    }

    return null;
  }

  /**
   * Returns the total income for a specific individual (iteration) identified by uuid parameter.
   *
   * @param submission submission containing input data to use
   * @param uuid       UUID of the iteration to pull the data from
   * @return a String containing an individuals total income.
   */
  public static String getIndividualsTotalIncome(Submission submission, String uuid) {
    DecimalFormat df = new DecimalFormat("0.00");

    if (submission.getInputData().containsKey(INCOME)) {
      ArrayList<Map<String, Object>> incomeSubflow =
          (ArrayList<Map<String, Object>>) submission.getInputData().get(INCOME);
      Map<String, Object> individualsIncomeEntry =
          incomeSubflow.stream()
              .filter(entry -> entry.get(ITERATION_UUID)
                  .equals(uuid))
              .toList()
              .get(0);
      ArrayList<String> incomeTypes = (ArrayList<String>) individualsIncomeEntry.get("incomeTypes[]");
      List<BigDecimal> incomeTypeAmounts = incomeTypes.stream()
          .map(type -> new BigDecimal((String) individualsIncomeEntry.get(type + "Amount")))
          .toList();
      return df.format(incomeTypeAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    return null;
  }

  /**
   * Returns the value of income for the whole family as a Double value. If the user has set the {@link SubmissionUtilities#REPORTED_TOTAL_ANNUAL_HOUSEHOLD_INCOME} value that will
   * be returned to the user instead of the total of the individual income entries.
   *
   * @param submission submission containing input data to use
   * @return total income amount as a Double
   */
  public static Double getHouseholdTotalIncomeValue(Submission submission) {
    Double total = 0.0;

    // if they entered it by hand that takes precedence over individual totals.
    if (submission.getInputData().containsKey(REPORTED_TOTAL_ANNUAL_HOUSEHOLD_INCOME)) {
      total = Double.valueOf((String) submission.getInputData().get(REPORTED_TOTAL_ANNUAL_HOUSEHOLD_INCOME));
    } else if (submission.getInputData().containsKey(INCOME)) {
      ArrayList<Map<String, Object>> incomeSubflow =
          (ArrayList<Map<String, Object>>) submission.getInputData().get(INCOME);
      ArrayList<Double> amounts = new ArrayList<>();
      incomeSubflow.forEach(iteration -> {
        iteration.forEach((key, value) -> {
          if (key.contains("Amount")) {
            amounts.add(Double.valueOf((String) value));
          }
        });
      });
      total = amounts.stream().reduce(0.0, (subtotal, element) -> subtotal + element);
    }

    return total;
  }

  /**
   * Returns the value of income for the whole family as a String value. If the user has set the {@link SubmissionUtilities#REPORTED_TOTAL_ANNUAL_HOUSEHOLD_INCOME} value that will
   * be returned to the user instead of the total of the individual income entries. The number 12000 will be returned formatted like so: 12,000.00
   *
   * @param submission submission containing input data to use
   * @return total income amount as a formatted numerical value
   */
  public static String getHouseholdTotalIncome(Submission submission) {
    return decimalFormat.format(getHouseholdTotalIncomeValue(submission));
  }

  /**
   * Returns the number of members in a family, including the applicant.
   *
   * @param submission submission containing input data to use
   * @return String containing family size
   */
  public static String getFamilySize(Submission submission) {
    //Add all household member and the applicant to get total family size
    int familySize = 1;
    if (submission.getInputData().get(HOUSEHOLD) != null) {
      var household = (ArrayList<LinkedHashMap<String, String>>) submission.getInputData().get(HOUSEHOLD);
      familySize = household.size() + familySize;
    }
    return (Integer.toString(familySize));
  }

  /**
   * Returns the max income threshold for a family of a certain size as an Integer.
   *
   * @param submission submission containing input data to use
   * @return an Integer containing the value
   */
  public static Integer getIncomeThresholdByFamilySizeValue(Submission submission) {
    Integer defaultThreshold = 116775 + ((Integer.parseInt(getFamilySize(submission)) - 8) * 11800);
    return switch (Integer.parseInt(getFamilySize(submission))) {
      case 1 -> 33975;
      case 2 -> 45775;
      case 3 -> 57575;
      case 4 -> 69375;
      case 5 -> 81175;
      case 6 -> 92975;
      case 7 -> 104775;
      case 8 -> 116775;
      default -> defaultThreshold;
    };
  }

  /**
   * Returns the max income threshold for a family of a certain size as a String value The string will be formatted for USD dollar amounts, meaning that the Integer value 33975
   * will be returned as "33,975"
   *
   * @param submission submission containing input data to use
   * @return a String containing the value
   */
  public static String getIncomeThresholdByFamilySize(Submission submission) {
    return decimalFormat.format(getIncomeThresholdByFamilySizeValue(submission));
  }

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

    if (!street2.isBlank()) {
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

