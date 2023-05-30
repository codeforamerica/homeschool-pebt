package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AssistanceProgramsPreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission) {
    var fields = new HashMap<String, SubmissionField>();

    if (submission.getInputData().get("householdMemberReceivesBenefits").equals("CalFresh")) {
      var caseNumber = (String) submission.getInputData().getOrDefault("householdMemberBenefitsCaseNumber", "");
      fields.put("calfresh", new SingleField("calfresh", "Yes", null));
      fields.put("program-case-number", new SingleField("program-case-number", caseNumber, null));
    }

    if (submission.getInputData().get("householdMemberReceivesBenefits").equals("CalWORKs")) {
      var caseNumber = (String) submission.getInputData().getOrDefault("householdMemberBenefitsCaseNumber", "");
      fields.put("calworks", new SingleField("calworks", "Yes", null));
      fields.put("program-case-number", new SingleField("program-case-number", caseNumber, null));
    }

    if (submission.getInputData().get("householdMemberReceivesBenefits").equals("FDPIR")) {
      var caseNumber = (String) submission.getInputData().getOrDefault("householdMemberBenefitsCaseNumberFDPIR", "");
      fields.put("fdpir", new SingleField("fdpir", "Yes", null));
      fields.put("program-case-number", new SingleField("program-case-number", caseNumber, null));
    }

    return fields;
  }
}
