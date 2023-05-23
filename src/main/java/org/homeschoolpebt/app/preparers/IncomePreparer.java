package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class IncomePreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission) {
    List<Map<String, Object>> incomes = (List<Map<String, Object>>) submission.getInputData().get("income");

    if (incomes != null) {
      var value = (String) incomes.get(0).get("incomeGrossMonthlyIndividual");
      return Map.of("totalIncome", new SingleField("totalIncome", value, null));
    } else {
      return Map.of();
    }
  }
}