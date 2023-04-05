package org.homeschoolpebt.app.submission.conditions;

import formflow.library.config.submission.Condition;
import formflow.library.data.Submission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AddressValidationIsOff implements Condition {

  @Value("${form-flow.address-validation.disabled:false}")
  private boolean isAddressValidationDisabled;

  @Override
  public Boolean run(Submission submission) {
    boolean addrValidationOffAtFragmentLevel = submission.getInputData().get("_validateresidentialAddress").equals("false");
    return isAddressValidationDisabled || addrValidationOffAtFragmentLevel;
  }
}