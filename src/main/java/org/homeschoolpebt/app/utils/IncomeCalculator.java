package org.homeschoolpebt.app.utils;

import formflow.library.data.Submission;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncomeCalculator {
  Submission submission;
  public IncomeCalculator(Submission submission) {
    this.submission = submission;
  }

  public BigDecimal totalUnearnedIncome() {
    var incomeTypes = (List<String>) submission.getInputData().getOrDefault("incomeTypes[]", new ArrayList<String>());
    var total = incomeTypes.stream()
      .map(type -> new BigDecimal((String) submission.getInputData().get(type + "Amount")))
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    return total;
  }
}
