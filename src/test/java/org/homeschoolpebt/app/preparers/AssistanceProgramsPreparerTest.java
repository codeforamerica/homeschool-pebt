package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AssistanceProgramsPreparerTest {

  @Test
  void includesCalFresh() {
    Submission submission = Submission.builder().inputData(Map.of(
      "householdMemberReceivesBenefits", "CalFresh",
      "householdMemberBenefitsCaseNumber", "ABC1234"
    )).build();

    AssistanceProgramsPreparer preparer = new AssistanceProgramsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "calfresh", new SingleField("calfresh", "Yes", null),
      "program-case-number", new SingleField("program-case-number", "ABC1234", null)
    ));
  }

  @Test
  void includesCalWorks() {
    Submission submission = Submission.builder().inputData(Map.of(
      "householdMemberReceivesBenefits", "CalWORKs",
      "householdMemberBenefitsCaseNumber", "ABC1234"
    )).build();

    AssistanceProgramsPreparer preparer = new AssistanceProgramsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
        "calworks", new SingleField("calworks", "Yes", null),
        "program-case-number", new SingleField("program-case-number", "ABC1234", null)
    ));
  }

  @Test
  void includesFDPIR() {
    Submission submission = Submission.builder().inputData(Map.of(
        "householdMemberReceivesBenefits", "FDPIR",
        "householdMemberBenefitsCaseNumberFDPIR", "ABC1234"
    )).build();

    AssistanceProgramsPreparer preparer = new AssistanceProgramsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
        "fdpir", new SingleField("fdpir", "Yes", null),
        "program-case-number", new SingleField("program-case-number", "ABC1234", null)
    ));
  }
}
