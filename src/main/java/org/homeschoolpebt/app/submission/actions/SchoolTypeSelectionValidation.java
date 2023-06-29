package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchoolTypeSelectionValidation implements Action {

  @Autowired
  MessageSource source;

  private final String HOMESCHOOL_INPUT = "studentHomeschoolAffidavitNumber";
  private final String VIRTUAL_INPUT = "studentVirtualSchoolName";

  public Map<String, List<String>> runValidation(FormSubmission formSubmission) {
    Map<String, List<String>> errorMessages = new HashMap<>();
    Map<String, Object> inputData = formSubmission.getFormData();
    String schoolType = (String) inputData.get("studentSchoolType");
    String homeschoolAffidavitNumber = (String) inputData.get(HOMESCHOOL_INPUT);
    String virtualSchoolName = (String) inputData.get(VIRTUAL_INPUT);

    if (schoolType.equalsIgnoreCase("homeschool") && (homeschoolAffidavitNumber == null || homeschoolAffidavitNumber.isEmpty())) {
      errorMessages.put(HOMESCHOOL_INPUT, List.of(source.getMessage("error.answer-this-question", null, LocaleContextHolder.getLocale())));
    }

    if (schoolType.equalsIgnoreCase("virtual") && (virtualSchoolName == null || virtualSchoolName.isEmpty())) {
      errorMessages.put(VIRTUAL_INPUT, List.of(source.getMessage("error.answer-this-question", null, LocaleContextHolder.getLocale())));
    }

    return errorMessages;
  }
}
