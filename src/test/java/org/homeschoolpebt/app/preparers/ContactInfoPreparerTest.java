package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactInfoPreparerTest {
  @InjectMocks ContactInfoPreparer preparer;
  @Mock private TransmissionRepository transmissionRepository;

  @Test
  void rendersAnUnvalidatedAddress() {
    Submission submission = Submission.builder().inputData(Map.of(
      "useValidatedResidentialAddress", "false",
      "residentialAddressStreetAddress1", "999 South St",
      "residentialAddressCity", "Los Agreles",
      "residentialAddressState", "CA",
      "residentialAddressZipCode", "90210"
    )).build();

    assertThat(preparer.prepareSubmissionFields(submission,null)).isEqualTo(Map.of(
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

    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.of(
      "address", new SingleField("address", "123 Main St, Apt B, San Fransokyo, CA", null),
      "zip-code", new SingleField("zip-code", "90210", null)
    ));
  }

  @Test
  void rendersStudentCheckbox() {
    Submission submission = Submission.builder().inputData(Map.of(
    "isApplyingForSelf", "true"
    )).build();

    assertThat(preparer.prepareSubmissionFields(submission, null)).contains(
      Map.entry("student", new SingleField("student", "Yes", null))
    );
  }

  @Test
  void rendersHouseholdMemberCheckbox() {
    Submission submission = Submission.builder().inputData(Map.of(
      "applicantIsInHousehold", "true"
    )).build();

    assertThat(preparer.prepareSubmissionFields(submission, null)).contains(
      Map.entry("household-member", new SingleField("household-member", "Yes", null))
    );
  }

  @Test
  void rendersAssisterCheckbox() {
    Submission submission = Submission.builder().inputData(Map.of(
      "applicantIsInHousehold", "false"
    )).build();

    assertThat(preparer.prepareSubmissionFields(submission, null)).contains(
      Map.entry("assister", new SingleField("assister", "Yes", null))
    );
  }

  @Test
  void rendersTheCaseNumber() {
    Submission submission = Submission.builder().flow("pebt").inputData(Map.of()).build();
    Transmission transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber("ABC1234");

    when(transmissionRepository.getTransmissionBySubmission(submission)).thenReturn(transmission);

    assertThat(preparer.prepareSubmissionFields(submission, null)).contains(
      Map.entry("case-number.case-number", new SingleField("case-number.case-number", transmission.getConfirmationNumber(), null))
    );
  }
}
