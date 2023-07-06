package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.springframework.stereotype.Component;

@Component
public class IsStudentTKto2 extends AbstractPebtCondition {
  public Boolean run(Submission submission, String uuid) {
    var student = currentSubflowItem(submission, "students", uuid);
    if (student == null) {
      return false;
    }

    var grade = student.getOrDefault("studentGrade", "");
    return grade.equals("TK") || grade.equals("K") || grade.equals("1st") || grade.equals("2nd");
  }
}
