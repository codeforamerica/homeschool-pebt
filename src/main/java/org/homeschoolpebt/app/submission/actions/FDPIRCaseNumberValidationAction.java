package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FDPIRCaseNumberValidationAction implements Action {

  private final String INPUT_NAME = "householdMemberReceivesBenefitsFDPIR";

  public Map<String, List<String>> runValidation(FormSubmission formSubmission) {
    Map<String, List<String>> errorMessages = new HashMap<>();
    Map<String, Object> inputData = formSubmission.getFormData();
    String caseNumberType = (String) inputData.get("householdMemberReceivesBenefits");


    if (caseNumberType == null || !caseNumberType.equalsIgnoreCase("FDPIR")) {
      return errorMessages;
    }

    if (inputData.get(INPUT_NAME) == null || inputData.get(INPUT_NAME).equals("")) {
      errorMessages.put(INPUT_NAME, List.of("household-receives-benefits.fdpir-provide-a-casenumber"));
    }

    return errorMessages;
  }

  private boolean isDateValid(String date) {
    try {
      DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");

      dtf.parseDateTime(date);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
