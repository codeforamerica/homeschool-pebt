package org.homeschoolpebt.app.preparers;

import static org.assertj.core.api.Assertions.assertThat;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ContactInfoPreparerTest {

  @Test
  void rendersAnUnvalidatedAddress() {
    Submission submission = Submission.builder().inputData(Map.of(
      "useValidatedResidentialAddress", "false",
      "residentialAddressStreetAddress1", "999 South St",
      "residentialAddressCity", "Los Agreles",
      "residentialAddressState", "CA",
      "residentialAddressZipCode", "90210"
    )).build();

    ContactInfoPreparer preparer = new ContactInfoPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "address", new SingleField("address", "999 South St, Los Agreles, CA", null),
      "zip-code", new SingleField("zip-code", "90210", null)
    ));
  }

  @Test
  void rendersAValidatedAddress() {
    Submission submission = Submission.builder().inputData(Map.of(
      "useValidatedResidentialAddress", "true",
      "residentialAddressStreetAddress1_validated", "123 Main St",
      "residentialAddressStreetAddress2_validated", "Apt B",
      "residentialAddressCity_validated", "San Fransokyo",
      "residentialAddressState_validated", "CA",
      "residentialAddressZipCode_validated", "90210"
    )).build();

    ContactInfoPreparer preparer = new ContactInfoPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).isEqualTo(Map.of(
      "address", new SingleField("address", "123 Main St, Apt B, San Fransokyo, CA", null),
      "zip-code", new SingleField("zip-code", "90210", null)
    ));
  }

  @Test
  void rendersStudentCheckbox() {
    Submission submission = Submission.builder().inputData(Map.of(
    "isApplyingForSelf", "true"
    )).build();

    ContactInfoPreparer preparer = new ContactInfoPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).contains(
      Map.entry("student", new SingleField("student", "Yes", null))
    );
  }

  @Test
  void rendersHouseholdMemberCheckbox() {
    Submission submission = Submission.builder().inputData(Map.of(
      "applicantIsInHousehold", "true"
    )).build();

    ContactInfoPreparer preparer = new ContactInfoPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).contains(
      Map.entry("household-member", new SingleField("household-member", "Yes", null))
    );
  }

  @Test
  void rendersAssisterCheckbox() {
    Submission submission = Submission.builder().inputData(Map.of(
      "applicantIsInHousehold", "false"
    )).build();

    ContactInfoPreparer preparer = new ContactInfoPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null, null)).contains(
      Map.entry("assister", new SingleField("assister", "Yes", null))
    );
  }
}
