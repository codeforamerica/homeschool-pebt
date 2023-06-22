package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.utils.IncomeCalculator;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HouseholdPreparer implements SubmissionFieldPreparer {

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();
    var data = submission.getInputData();

    var memberIndex = 1;

    // submitter (if they're in the household)
    if (data.getOrDefault("applicantIsInHousehold", "false").equals("true")) {
      var name = SubmissionUtilities.applicantFullName(submission);
      var totalFutureIncome = totalFutureIncomeForMember(submission, name);
      addMember(fields, name, totalFutureIncome, false, memberIndex);
      memberIndex += 1;
    }

    // students
    var students = (List<Map<String, String>>) submission.getInputData().getOrDefault("students", new ArrayList<HashMap<String, Object>>());
    for (var student : students) {
      var name = SubmissionUtilities.studentFullName(student);
      var totalFutureIncome = totalFutureIncomeForMember(submission, name);
      addMember(fields, name, totalFutureIncome, true, memberIndex);
      memberIndex += 1;
    }

    // other hh members
    var householdMembers = (List<Map<String, String>>) submission.getInputData().getOrDefault("household", new ArrayList<HashMap<String, Object>>());
    for (var householdMember : householdMembers) {
      var name = SubmissionUtilities.householdMemberFullName(householdMember);
      var totalFutureIncome = totalFutureIncomeForMember(submission, name);
      addMember(fields, name, totalFutureIncome, false, memberIndex);
      memberIndex += 1;
    }

    return fields;
  }

  private Double totalFutureIncomeForMember(Submission submission, String memberName) {
    var incomes = (List<HashMap<String, Object>>) submission.getInputData().getOrDefault("income", new ArrayList<HashMap<String, Object>>());
    return incomes
      .stream()
      .filter(j -> j.get("incomeMember").equals(memberName))
      .map(IncomeCalculator::futureIncomeForJob)
      .reduce(0.0d, Double::sum);
  }

  private void addMember(Map<String, SubmissionField> fields, String name, Double futureIncome, Boolean isStudent, Integer i) {
    setField(fields, "hhmember%s".formatted(i), name);
    setField(fields, "hhmember%s-future-income".formatted(i), SubmissionUtilities.formatMoney(futureIncome));
    setField(fields, "hhmember%s-student".formatted(i), isStudent ? "Yes" : "No");
  }

  private void setField(Map<String, SubmissionField> fields, String key, String value) {
    fields.put(key, new SingleField(key, value, null));
  }
}
