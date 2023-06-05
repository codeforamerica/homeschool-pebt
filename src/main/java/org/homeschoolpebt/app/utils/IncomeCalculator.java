package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncomeCalculator {
  Submission submission;
  public IncomeCalculator(Submission submission) {
    this.submission = submission;
  }

  public Double totalUnearnedIncome() {
    var incomeTypes = (List<String>) submission.getInputData().getOrDefault("incomeTypes[]", new ArrayList<String>());
    var total = incomeTypes.stream()
      .map(type -> Double.parseDouble(submission.getInputData().get(type + "Amount").toString()))
      .reduce(0.0d, Double::sum);

    return total;
  }

  public Double totalPastEarnedIncome() {
    var jobs = (List<Map<String, Object>>) submission.getInputData().getOrDefault("income", new ArrayList<Map<String, Object>>());
    var total = jobs.stream()
      .map(job -> pastIncomeForJob(job))
      .reduce(0.0d, Double::sum);

    return total;
  }

  public Double totalFutureEarnedIncome() {
    var jobs = (List<Map<String, Object>>) submission.getInputData().getOrDefault("income", new ArrayList<Map<String, Object>>());
    var total = jobs.stream()
      .map(IncomeCalculator::futureIncomeForJob)
      .reduce(0.0d, Double::sum);

    return total;
  }

  public static Double pastIncomeForJob(Map<String, Object> job) {
    if (job.getOrDefault("incomeSelfEmployed", "false").toString().equals("true")) {
      return SubmissionUtilities.getSelfEmployedNetIncomeAmount(job, SubmissionUtilities.TimePeriod.MONTHLY);
    } else if (job.getOrDefault("incomeIsJobHourly", "").toString().equals("true")) {
      return SubmissionUtilities.getHourlyGrossIncomeAmount(job);
    } else {
      return SubmissionUtilities.getRegularPayAmount(job);
    }
  }

  public static Double futureIncomeForJob(Map<String, Object> job) {
    if (job.getOrDefault("incomeWillBeLess", "false").toString().equals("true")) {
      if (job.getOrDefault("incomeSelfEmployed", "false").toString().equals("true")) {
        var annual = Double.parseDouble(job.get("incomeCustomAnnualIncome").toString());
        return annual / 12;
      } else {
        // TODO: Implement this once we build pages for this in hourly/regularly
        return pastIncomeForJob(job);
      }
    } else {
      return pastIncomeForJob(job);
    }
  }
}
