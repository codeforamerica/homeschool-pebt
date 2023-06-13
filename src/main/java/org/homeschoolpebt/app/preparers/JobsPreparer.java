package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.utils.IncomeCalculator;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobsPreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
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
    fields.put("name", job.getOrDefault("incomeJobName", "").toString());
    fields.put("future-pay-comments", job.getOrDefault("incomeWillBeLessDescription", "").toString());

    // TODO: Move this into IncomeCalculator
    if (job.getOrDefault("incomeSelfEmployed", "false").toString().equals("true")) {
      var lastMonthNetPay = SubmissionUtilities.getSelfEmployedNetIncomeAmount(job, SubmissionUtilities.TimePeriod.MONTHLY);
      fields.put("past-monthly-pay", SubmissionUtilities.formatMoney(lastMonthNetPay));

      if (SubmissionUtilities.useSelfEmploymentCustomExpenses(job)) {
        fields.put("pay-type", "Net Income (After Expenses)");
        fields.put("past-monthly-pay-calculation", "%s Gross Income - %s Expenses".formatted(
          SubmissionUtilities.formatMoney((String) job.get("incomeGrossMonthlyIndividual")),
          SubmissionUtilities.formatMoney((String) job.get("incomeSelfEmployedOperatingExpenses"))
        ));
      } else {
        fields.put("pay-type", "Net Income (40% Deduction)");
        fields.put("past-monthly-pay-calculation", "%s Gross Income".formatted(SubmissionUtilities.formatMoney((String) job.get("incomeGrossMonthlyIndividual"))));
      }
    } else if (job.getOrDefault("incomeIsJobHourly", "").toString().equals("true")) {
      var pastPay = SubmissionUtilities.getHourlyGrossIncomeAmount(job);
      var pastPayCalculation = SubmissionUtilities.getHourlyGrossIncomeExplanation(job);

      fields.put("past-monthly-pay", SubmissionUtilities.formatMoney(pastPay));
      fields.put("past-monthly-pay-calculation", pastPayCalculation);
      fields.put("pay-type", "Gross Income");
    } else {
      var pastPay = SubmissionUtilities.getRegularPayAmount(job);
      var pastPayCalculation = SubmissionUtilities.getRegularPayExplanation(job);

      fields.put("past-monthly-pay", SubmissionUtilities.formatMoney(pastPay));
      fields.put("past-monthly-pay-calculation", pastPayCalculation);
      fields.put("pay-type", "Gross Income");
    }

    if (job.getOrDefault("incomeWillBeLess", "false").toString().equals("true")) {
      var futureIncome = IncomeCalculator.futureIncomeForJob(job);
      fields.put("future-monthly-pay", SubmissionUtilities.formatMoney(futureIncome));
    }

    return fields;
  }
}
