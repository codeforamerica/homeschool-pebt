package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HasUnearnedIncome extends AbstractPebtCondition {
  public Boolean run(Submission submission) {
    if (submission == null) {
      return false;
    }

    var inputData = submission.getInputData();
    if (inputData == null) {
      return false;
    }

    return hasContents(inputData.get("incomeUnearnedTypes[]")) || hasContents(inputData.get("incomeUnearnedRetirementTypes[]"));
  }

  private boolean hasContents(Object o) {
    if (o == null) {
      return false;
    }

    if (!(o instanceof List)) {
      return false;
    }

    var l = (List) o;
    if (l.size() == 0 || (l.size() == 1 && (l.get(0) == null || l.get(0).equals("none")))) {
      return false;
    } else {
      return true;
    }
  }
}
