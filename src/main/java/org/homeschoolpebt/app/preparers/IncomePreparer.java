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
import java.util.Map;

@Component
public class IncomePreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, Map<String, Object> data, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();
    var calc = new IncomeCalculator(submission);

    // household count
    fields.put("household-count", new SingleField("household-count", SubmissionUtilities.getHouseholdMemberCount(submission).toString(), null));

    // unearned
    fields.put("income-unemployment", new SingleField("income-unemployment", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeUnemploymentAmount")), null));
    fields.put("income-workers-comp", new SingleField("income-workers-comp", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeWorkersCompensationAmount")), null));
    fields.put("income-spousal-support", new SingleField("income-spousal-support", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeSpousalSupportAmount")), null));
    fields.put("income-child-support", new SingleField("income-child-support", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeChildSupportAmount")), null));
    fields.put("income-pension", new SingleField("income-pension", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomePensionAmount")), null));
    fields.put("income-retirement", new SingleField("income-retirement", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeRetirementAmount")), null));
    fields.put("income-ssi", new SingleField("income-ssi", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeSSIAmount")), null));
    fields.put("income-other", new SingleField("income-other", SubmissionUtilities.formatMoney((String) submission.getInputData().get("incomeOtherAmount")), null));
    var totalUnearnedIncome = calc.totalUnearnedIncome();
    fields.put("income-hh-unearned", new SingleField("income-hh-unearned", SubmissionUtilities.formatMoney(totalUnearnedIncome), null));

    // earned
    var futureEarned = calc.totalFutureEarnedIncome();
    fields.put("income-hh-future-earned", new SingleField("income-hh-future-earned", SubmissionUtilities.formatMoney(futureEarned), null));
    var pastEarned = calc.totalPastEarnedIncome();
    fields.put("income-hh-past-earned", new SingleField("income-hh-past-earned", SubmissionUtilities.formatMoney(pastEarned), null));
    var futureTotal = futureEarned + totalUnearnedIncome;
    fields.put("income-hh-future-total", new SingleField("income-hh-future-total", SubmissionUtilities.formatMoney(futureTotal), null));
    var pastTotal = pastEarned + totalUnearnedIncome;
    fields.put("income-hh-past-total", new SingleField("income-hh-past-total", SubmissionUtilities.formatMoney(pastTotal), null));

    return fields;
  }
}
