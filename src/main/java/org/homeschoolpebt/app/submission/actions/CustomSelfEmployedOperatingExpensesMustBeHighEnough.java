package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.homeschoolpebt.app.utils.SubmissionUtilities.decimalFormatWithoutComma;

@Slf4j
@Component
public class CustomSelfEmployedOperatingExpensesMustBeHighEnough implements Action {

  @Autowired
  MessageSource source;

  private final String INPUT_NAME = "incomeSelfEmployedOperatingExpenses";
  private final String MIN_VALUE_INPUT_NAME = "incomeTransientStandardOperatingExpensesAmount";

  public Map<String, List<String>> runValidation(FormSubmission formSubmission) {
    Map<String, Object> inputData = formSubmission.getFormData();
    boolean incomeSelfEmployedCustomOperatingExpenses = isTrue((String) inputData.get("incomeSelfEmployedCustomOperatingExpenses"));
    if (!incomeSelfEmployedCustomOperatingExpenses) {
      return new HashMap<>();
    }

    Double incomeSelfEmployedOperatingExpenses = asNumber((String) inputData.get(INPUT_NAME));
    if (incomeSelfEmployedOperatingExpenses == null) {
      return new HashMap<>();
    }

    Double minValue = asNumber((String) inputData.get(MIN_VALUE_INPUT_NAME));
    if (minValue == null) {
      return new HashMap<>();
    }

    if (incomeSelfEmployedOperatingExpenses < minValue) {
      return Map.ofEntries(entry(INPUT_NAME, List.of(
        source.getMessage("income-self-employed-operating-expenses.operating-expenses-too-low", new String[]{decimalFormatWithoutComma.format(minValue)}, LocaleContextHolder.getLocale())
      )));
    }

    return new HashMap<>();
  }

  private boolean isTrue(String value) {
    return (value != null) && value.equals("true");
  }

  private Double asNumber(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException _e) {
      return null;
    }
  }
}
