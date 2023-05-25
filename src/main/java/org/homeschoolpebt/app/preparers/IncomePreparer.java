package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class IncomePreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission) {
    var fields = new HashMap<String, SubmissionField>();

    // unearned
    fields.put("income-unemployment", new SingleField("income-unemployment", (String) submission.getInputData().get("incomeUnemploymentAmount"), null));
    fields.put("income-workers-comp", new SingleField("income-workers-comp", (String) submission.getInputData().get("incomeWorkersCompensationAmount"), null));
    fields.put("income-spousal-support", new SingleField("income-spousal-support", (String) submission.getInputData().get("incomeSpousalSupportAmount"), null));
    fields.put("income-child-support", new SingleField("income-child-support", (String) submission.getInputData().get("incomeChildSupportAmount"), null));
    fields.put("income-pension", new SingleField("income-pension", (String) submission.getInputData().get("incomePensionAmount"), null));
    fields.put("income-retirement", new SingleField("income-retirement", (String) submission.getInputData().get("incomeRetirementAmount"), null));
    fields.put("income-ssi", new SingleField("income-ssi", (String) submission.getInputData().get("incomeSSIAmount"), null));
    fields.put("income-other", new SingleField("income-other", (String) submission.getInputData().get("incomeOtherAmount"), null));

    // TODO: earned

    return fields;
  }
}
