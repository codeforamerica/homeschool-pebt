package org.homeschoolpebt.app.submission.actions;

import formflow.library.config.submission.Action;
import formflow.library.data.FormSubmission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClearOtherCaseNumberFields implements Action {

  private static final String CALFRESH = "Calfresh";
  private static final String CALWORKS = "CalWORKs";
  private static final String FDPIR = "FDPIR";

  public void run(FormSubmission formsubmission) {
    String selection = (String) formsubmission.getFormData().get("householdMemberReceivesBenefits");

    if (CALFRESH.equals(selection)) {
      formsubmission.getFormData().put("householdMemberBenefitsCaseNumberCalworks", "");
      formsubmission.getFormData().put("householdMemberBenefitsCaseNumberFDPIR", "");
    } else if (CALWORKS.equals(selection)) {
      formsubmission.getFormData().put("householdMemberBenefitsCaseNumberCalfresh", "");
      formsubmission.getFormData().put("householdMemberBenefitsCaseNumberFDPIR", "");
    } else if (FDPIR.equals(selection)) {
      formsubmission.getFormData().put("householdMemberBenefitsCaseNumberCalfresh", "");
      formsubmission.getFormData().put("householdMemberBenefitsCaseNumberCalworks", "");
    }
  }
}
