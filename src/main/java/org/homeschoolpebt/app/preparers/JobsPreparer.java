package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.inputs.Pebt;
import org.homeschoolpebt.app.utils.IncomeCalculator;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JobsPreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, Map<String, Object> data, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();

    var pebt = Pebt.fromSubmission(submission);
    var jobs = pebt.getIncome();
    if (jobs == null) {
      return Map.of();
    }

    var jobIndex = 1;
    for (var job : jobs) {
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

  private HashMap<String, String> jobFields(Pebt.Income job) {
    var fields = new HashMap<String, String>();
    fields.put("employee-name", job.getIncomeMember());
    fields.put("name", job.getIncomeJobName());
    fields.put("future-pay-comments", job.getIncomeWillBeLessDescription());

    // TODO: Move this into IncomeCalculator
    if (job.getIncomeSelfEmployed().equals("true")) {
      var lastMonthNetPay = SubmissionUtilities.getSelfEmployedNetIncomeAmount(job, SubmissionUtilities.TimePeriod.MONTHLY);
      fields.put("past-monthly-pay", SubmissionUtilities.formatMoney(lastMonthNetPay));

      if (SubmissionUtilities.useSelfEmploymentCustomExpenses(job)) {
        fields.put("pay-type", "Net Income (After Expenses)");
        fields.put("past-monthly-pay-calculation", "%s Gross Income - %s Expenses".formatted(
          SubmissionUtilities.formatMoney(job.getIncomeGrossMonthlyIndividual()),
          SubmissionUtilities.formatMoney(job.getIncomeSelfEmployedOperatingExpenses())
        ));
      } else {
        fields.put("pay-type", "Net Income (40% Deduction)");
        fields.put("past-monthly-pay-calculation", "%s Gross Income".formatted(SubmissionUtilities.formatMoney(job.getIncomeGrossMonthlyIndividual())));
      }
    } else if (job.getIncomeIsJobHourly().equals("true")) {
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

    if (job.getIncomeWillBeLess().equals("true")) {
      var futureIncome = IncomeCalculator.futureIncomeForJob(job);
      fields.put("future-monthly-pay", SubmissionUtilities.formatMoney(futureIncome));
    }

    return fields;
  }
}
