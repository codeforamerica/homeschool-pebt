package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AssistanceProgramsPreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();

    if ("CalFresh".equals(submission.getInputData().get("householdMemberReceivesBenefits"))) {
      var caseNumber = (String) submission.getInputData().getOrDefault("householdMemberBenefitsCaseNumberCalfresh", "");
      fields.put("calfresh", new SingleField("calfresh", "Yes", null));
      fields.put("program-case-number", new SingleField("program-case-number", caseNumber, null));
    }

    if ("CalWORKs".equals(submission.getInputData().get("householdMemberReceivesBenefits"))) {
      var caseNumber = (String) submission.getInputData().getOrDefault("householdMemberBenefitsCaseNumberCalworks", "");
      fields.put("calworks", new SingleField("calworks", "Yes", null));
      fields.put("program-case-number", new SingleField("program-case-number", caseNumber, null));
    }

    if ("FDPIR".equals(submission.getInputData().get("householdMemberReceivesBenefits"))) {
      var caseNumber = (String) submission.getInputData().getOrDefault("householdMemberBenefitsCaseNumberFDPIR", "");
      fields.put("fdpir", new SingleField("fdpir", "Yes", null));
      fields.put("program-case-number", new SingleField("program-case-number", caseNumber, null));
    }

    return fields;
  }
}
