package org.homeschoolpebt.app.submission.conditions;

import formflow.library.data.Submission;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HasUnearnedIncomeTest {

  @Test
  void testReturnsFalseWhenUnearnedIncomeTypesAreNull() {
    var submission = Submission.builder().inputData(Map.ofEntries()).build();
    var subject = new HasUnearnedIncome();
    assertThat(subject.run(submission)).isEqualTo(false);
  }

  @Test
  void testReturnsFalseWhenUnearnedIncomeTypesAreEmptyArrays() {
    var submission = Submission.builder().inputData(Map.ofEntries(Map.entry("incomeUnearnedTypes[]", List.of()), Map.entry("incomeUnearnedRetirementTypes[]", List.of()))).build();
    var subject = new HasUnearnedIncome();
    assertThat(subject.run(submission)).isEqualTo(false);
  }

  @Test
  void testReturnsFalseWhenArraysContainStringNone() {
    var submission = Submission.builder().inputData(Map.ofEntries(Map.entry("incomeUnearnedTypes[]", List.of("none")), Map.entry("incomeUnearnedRetirementTypes[]", List.of("none")))).build();
    var subject = new HasUnearnedIncome();
    assertThat(subject.run(submission)).isEqualTo(false);
  }

  @Test
  void testReturnsTrueWhenArraysContainOtherStrings() {
    var submission = Submission.builder().inputData(Map.ofEntries(Map.entry("incomeUnearnedTypes[]", List.of("none")), Map.entry("incomeUnearnedRetirementTypes[]", List.of("income401k403b")))).build();
    var subject = new HasUnearnedIncome();
    assertThat(subject.run(submission)).isEqualTo(true);
  }
}
