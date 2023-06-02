package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.inputs.Pebt;

public class IncomeCalculator {
  Pebt pebt;
  public IncomeCalculator(Submission submission) {
    this.pebt = Pebt.fromSubmission(submission);
  }

  public Double totalUnearnedIncome() {
    var incomeTypes = pebt.getIncomeTypes();
    if (incomeTypes == null) {
      return 0.0;
    }

    var total = incomeTypes.stream().map(type -> switch (type) {
        case incomeUnemployment -> pebt.getIncomeUnemploymentAmount();
        case incomeWorkersCompensation -> pebt.getIncomeWorkersCompensationAmount();
        case incomeSpousalSupport -> pebt.getIncomeSpousalSupportAmount();
        case incomeChildSupport -> pebt.getIncomeChildSupportAmount();
        case incomePension -> pebt.getIncomePensionAmount();
        case incomeRetirement -> pebt.getIncomeRetirementAmount();
        case incomeSSI -> pebt.getIncomeSSIAmount();
        case incomeOther -> pebt.getIncomeOtherAmount();
      })
      .map(Double::parseDouble)
      .reduce(0.0d, Double::sum);

    return total;
  }

  public Double totalPastEarnedIncome() {
    if (pebt.getIncome() == null) {
      return 0.0;
    }

    var total = pebt.getIncome().stream()
      .map(IncomeCalculator::pastIncomeForJob)
      .reduce(0.0d, Double::sum);

    return total;
  }

  public Double totalFutureEarnedIncome() {
    if (pebt.getIncome() == null) {
      return 0.0;
    }

    var total = pebt.getIncome().stream()
      .map(job -> {
        if (job.getIncomeWillBeLess().equals("true")) {
          return futureIncomeForJob(job);
        } else {
          return pastIncomeForJob(job);
        }
      })
      .reduce(0.0d, Double::sum);

    return total;
  }

  public static Double pastIncomeForJob(Pebt.Income job) {
    if (job.getIncomeSelfEmployed().equals("true")) {
      return SubmissionUtilities.getSelfEmployedNetIncomeAmount(job, SubmissionUtilities.TimePeriod.MONTHLY);
    } else if (job.getIncomeIsJobHourly().equals("true")) {
      return SubmissionUtilities.getHourlyGrossIncomeAmount(job);
    } else {
      return SubmissionUtilities.getRegularPayAmount(job);
    }
  }

  public static Double futureIncomeForJob(Pebt.Income job) {
    var annual = Double.parseDouble(job.getIncomeCustomAnnualIncome());
    return annual / 12;
  }
}
