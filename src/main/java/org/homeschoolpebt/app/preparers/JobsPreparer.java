package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobsPreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, Map<String, Object> data, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();

    var jobs = submission.getInputData().get("income");
    if (jobs == null) {
      return Map.of();
    }

    var jobIndex = 1;
    for (var job : (List<Map<String, Object>>) jobs) {
      var jobFields = jobFields(job);
      for (var entry : jobFields.entrySet()) {
        // e.g. job1-name
        var fieldName = "job%s-%s".formatted(jobIndex, entry.getKey());
        var fieldValue = (String) entry.getValue();

        fields.put(
          fieldName,
          new SingleField(fieldName, fieldValue, null)
        );
      }

      jobIndex += 1;
    }
    return fields;
  }

  private HashMap<String, String> jobFields(Map<String, Object> job) {
    var fields = new HashMap<String, String>();
    fields.put("employee-name", job.getOrDefault("incomeMember", "").toString());

    // TODO: Move this into IncomeCalculator
    if (job.getOrDefault("incomeSelfEmployed", "false").toString().equals("true")) {
      var lastMonthNetPay = SubmissionUtilities.getSelfEmployedNetIncomeAmount(job, SubmissionUtilities.TimePeriod.MONTHLY);
      fields.put("past-monthly-pay", lastMonthNetPay);
      fields.put("past-monthly-pay-calculation", SubmissionUtilities.formatMoney((String) job.get("incomeGrossMonthlyIndividual")) + " Gross Monthly Income");

      if (SubmissionUtilities.selfEmploymentCustomExpenses(job)) {
        fields.put("pay-type", "Net Income (Custom Deductions)");
      } else {
        fields.put("pay-type", "Net Income (40% Deduction)");
      }

      if (job.getOrDefault("incomeWillBeLess", "false").toString().equals("true")) {
        var annual = Double.parseDouble(job.get("incomeCustomAnnualIncome").toString());
        fields.put("future-monthly-pay", SubmissionUtilities.formatMoney(annual / 12));
        fields.put("future-pay-comments", job.getOrDefault("incomeWillBeLessDescription", "").toString());
      }
    } else {
    }

    fields.put("name", job.getOrDefault("incomeJobName", "").toString());

    return fields;
  }
}
