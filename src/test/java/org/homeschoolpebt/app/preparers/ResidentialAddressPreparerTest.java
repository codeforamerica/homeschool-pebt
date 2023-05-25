package org.homeschoolpebt.app.preparers;

import static org.assertj.core.api.Assertions.assertThat;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ResidentialAddressPreparerTest {

  @Test
  void rendersAnUnvalidatedAddress() {
    Submission submission = Submission.builder().inputData(Map.of(
      "useValidatedResidentialAddress", "false",
      "residentialAddressStreetAddress1", "999 South St",
      "residentialAddressCity", "Los Agreles",
      "residentialAddressState", "CA",
      "residentialAddressZipCode", "90210"
    )).build();

    ResidentialAddressPreparer preparer = new ResidentialAddressPreparer();
    assertThat(preparer.prepareSubmissionFields(submission)).isEqualTo(Map.of(
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

    ResidentialAddressPreparer preparer = new ResidentialAddressPreparer();
    assertThat(preparer.prepareSubmissionFields(submission)).isEqualTo(Map.of(
      "address", new SingleField("address", "123 Main St, Apt B, San Fransokyo, CA", null),
      "zip-code", new SingleField("zip-code", "90210", null)
    ));
  }
}
