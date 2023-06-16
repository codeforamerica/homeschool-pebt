package org.homeschoolpebt.app.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolListUtilitiesTest {
  @Test
  void allCepSchoolsTestACoupleValues() {
    assertThat(SchoolListUtilities.allCepSchools(new ArrayList(List.of("1612590135905")))).isTrue();
    assertThat(SchoolListUtilities.allCepSchools(new ArrayList(List.of("1612590135905", "01612590135905")))).isTrue();
    assertThat(SchoolListUtilities.allCepSchools(new ArrayList(List.of("1612590135905", "01612590135905", "1234")))).isFalse();
    assertThat(SchoolListUtilities.allCepSchools(new ArrayList(List.of("1612590135905", "57727106071278")))).isTrue();
  }
}
