package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.utils.IncomeCalculator;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IncomePreparer implements SubmissionFieldPreparer {
  private Map<String, String> getUnearnedIncomeFieldNamesByPdfFieldName() {
    return new TreeMap<>() {
      {
        put("income-unemployment", "incomeUnemployment");
        put("income-workers-comp", "incomeWorkersCompensation");
        put("income-spousal-support", "incomeSpousalSupport");
        put("income-child-support", "incomeChildSupport");
        put("income-disability", "incomeDisability");
        put("income-veterans", "incomeVeterans");
        put("income-other", "incomeOther");
      }
    };
  }

  private Map<String, String> getUnearnedRetirementIncomeFieldNamesByPdfFieldName() {
    return new TreeMap<>() {
      {
        put("income-ssi", "incomeSSI");
        put("income-pension", "incomePension");
        put("income-social-security", "incomeSocialSecurity");
        put("income-401k", "income401k403b");
      }
    };
  }

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();
    var calc = new IncomeCalculator(submission);

    // household count
    fields.put("household-count", new SingleField("household-count", SubmissionUtilities.getHouseholdMemberCount(submission).toString(), null));

    // unearned
    double totalUnearnedIncome = 0;
    for (var entry : getUnearnedIncomeFieldNamesByPdfFieldName().entrySet()) {
      var pdfFieldName = entry.getKey();
      var submissionFieldName = entry.getValue();
      var typesCheckedByClient = (List<String>) submission.getInputData().getOrDefault("incomeUnearnedTypes[]", new ArrayList<String>());
      var submissionAmountFieldName = entry.getValue() + "Amount";
      var inputDataFieldValue = typesCheckedByClient.contains(submissionFieldName) ? (String) submission.getInputData().get(submissionAmountFieldName) : null;
      fields.put(pdfFieldName, new SingleField(pdfFieldName, SubmissionUtilities.formatMoney(inputDataFieldValue), null));
      totalUnearnedIncome += parseDoubleWithNullAsZero(inputDataFieldValue);
    }

    // unearned (retirement)
    for (var entry : getUnearnedRetirementIncomeFieldNamesByPdfFieldName().entrySet()) {
      var pdfFieldName = entry.getKey();
      var submissionFieldName = entry.getValue();
      var typesCheckedByClient = (List<String>) submission.getInputData().getOrDefault("incomeUnearnedRetirementTypes[]", new ArrayList<String>());
      var submissionAmountFieldName = entry.getValue() + "Amount";
      var inputDataFieldValue = typesCheckedByClient.contains(submissionFieldName) ? (String) submission.getInputData().get(submissionAmountFieldName) : null;
      fields.put(pdfFieldName, new SingleField(pdfFieldName, SubmissionUtilities.formatMoney(inputDataFieldValue), null));
      totalUnearnedIncome += parseDoubleWithNullAsZero(inputDataFieldValue);
    }

    fields.put("income-hh-unearned", new SingleField("income-hh-unearned", SubmissionUtilities.formatMoney(totalUnearnedIncome), null));
    fields.put("income-unearned-comments", new SingleField("income-unearned-comments", (String) submission.getInputData().get("incomeUnearnedDescription"), null));

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

  private double parseDoubleWithNullAsZero(Object o) {
    if (o == null) {
      return 0;
    }
    return Double.parseDouble(o.toString());
  }
}
