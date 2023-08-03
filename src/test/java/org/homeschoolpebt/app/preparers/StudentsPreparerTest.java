package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.SingleField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StudentsPreparerTest {
  @Test
  void includesStudentFields() {
    HashMap<String, Object> studentFields = new HashMap<>() {{
      put("studentFirstName", "firsty");
      put("studentMiddleInitial", "i");
      put("studentLastName", "lastnameson");
      put("studentVirtualSchoolName", "Some Virtual Academy");
      put("studentUnenrolledSchoolName", "37680230135277 - Muraoka (Saburo) Elementary (Chula Vista Elementary)");
      put("studentWouldAttendSchoolName", "38684786040935 - Thomas Edison Charter Academy (San Francisco Unified)");
      put("studentDesignations[]", List.of("foster", "unhoused", "migrant", "runaway"));
      put("studentHomeschoolAffidavitNumber", "12345678901234");
      put("studentGrade", "9th");
      put("studentBirthdayDate", "01/02/1991");
    }};

    Submission submission = Submission.builder().inputData(Map.of(
      "students", List.of(studentFields)
    )).build();

    StudentsPreparer preparer = new StudentsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.ofEntries(
      Map.entry("student1-first-name", new SingleField("student1-first-name", "firsty", null)),
      Map.entry("student1-middle-initial", new SingleField("student1-middle-initial", "i", null)),
      Map.entry("student1-last-name", new SingleField("student1-last-name", "lastnameson", null)),
      Map.entry("student1-withdrawn-school-code", new SingleField("student1-withdrawn-school-code", "37680230135277", null)),
      Map.entry("student1-withdrawn-school-district", new SingleField("student1-withdrawn-school-district", "Chula Vista Elementary", null)),
      Map.entry("student1-withdrawn-school", new SingleField("student1-withdrawn-school", "Muraoka (Saburo) Elementary", null)),
      Map.entry("student1-withdrawn-school-cep", new SingleField("student1-withdrawn-school-cep", "Yes", null)),
      Map.entry("student1-anticipated-school-code", new SingleField("student1-anticipated-school-code", "38684786040935", null)),
      Map.entry("student1-anticipated-school-district", new SingleField("student1-anticipated-school-district", "San Francisco Unified", null)),
      Map.entry("student1-anticipated-school-cep", new SingleField("student1-anticipated-school-cep", "No", null)),
      Map.entry("student1-anticipated-school", new SingleField("student1-anticipated-school", "Thomas Edison Charter Academy", null)),
      Map.entry("student1-virtual-school", new SingleField("student1-virtual-school", "Some Virtual Academy", null)),
      Map.entry("student1-migrant", new SingleField("student1-migrant", "Yes", null)),
      Map.entry("student1-unhoused", new SingleField("student1-unhoused", "Yes", null)),
      Map.entry("student1-foster", new SingleField("student1-foster", "Yes", null)),
      Map.entry("student1-runaway", new SingleField("student1-runaway", "Yes", null)),
      Map.entry("student1-homeschool", new SingleField("student1-homeschool", "12345678901234", null)),
      Map.entry("student1-grade", new SingleField("student1-grade", "9th", null)),
      Map.entry("student1-dob", new SingleField("student1-dob", "01/02/1991", null))
    ));
  }

  @Test
  void includesStudentFieldsEvenWhenNoUnenrolledSchool() {
    HashMap<String, Object> studentFields = new HashMap<>() {{
      put("studentFirstName", "firsty");
      put("studentMiddleInitial", "i");
      put("studentLastName", "lastnameson");
      put("studentVirtualSchoolName", "Some Virtual Academy");
      put("hasUnenrolled", "false");
      put("studentWouldAttendSchoolName", "38684786040935 - Thomas Edison Charter Academy (San Francisco Unified)");
      put("studentDesignations[]", List.of("foster", "unhoused", "migrant", "runaway"));
      put("studentHomeschoolAffidavitNumber", "12345678901234");
      put("studentGrade", "9th");
      put("studentBirthdayDate", "01/02/1991");
    }};

    Submission submission = Submission.builder().inputData(Map.of(
      "students", List.of(studentFields)
    )).build();

    StudentsPreparer preparer = new StudentsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).isEqualTo(Map.ofEntries(
      Map.entry("student1-first-name", new SingleField("student1-first-name", "firsty", null)),
      Map.entry("student1-middle-initial", new SingleField("student1-middle-initial", "i", null)),
      Map.entry("student1-last-name", new SingleField("student1-last-name", "lastnameson", null)),
      Map.entry("student1-anticipated-school-code", new SingleField("student1-anticipated-school-code", "38684786040935", null)),
      Map.entry("student1-anticipated-school-district", new SingleField("student1-anticipated-school-district", "San Francisco Unified", null)),
      Map.entry("student1-anticipated-school-cep", new SingleField("student1-anticipated-school-cep", "No", null)),
      Map.entry("student1-anticipated-school", new SingleField("student1-anticipated-school", "Thomas Edison Charter Academy", null)),
      Map.entry("student1-virtual-school", new SingleField("student1-virtual-school", "Some Virtual Academy", null)),
      Map.entry("student1-migrant", new SingleField("student1-migrant", "Yes", null)),
      Map.entry("student1-unhoused", new SingleField("student1-unhoused", "Yes", null)),
      Map.entry("student1-foster", new SingleField("student1-foster", "Yes", null)),
      Map.entry("student1-runaway", new SingleField("student1-runaway", "Yes", null)),
      Map.entry("student1-homeschool", new SingleField("student1-homeschool", "12345678901234", null)),
      Map.entry("student1-grade", new SingleField("student1-grade", "9th", null)),
      Map.entry("student1-dob", new SingleField("student1-dob", "01/02/1991", null))
    ));
  }

  @Test
  void handlesCustomSchoolEntries() {
    HashMap<String, Object> studentFields = new HashMap<>() {{
      put("studentFirstName", "firsty");
      put("studentLastName", "lastnameson");
      put("studentVirtualSchoolName", "Some Virtual Academy");
      put("studentUnenrolledSchoolName", "Other In Person School");
      put("studentWouldAttendSchoolName", "Yet another In Person School");
    }};

    Submission submission = Submission.builder().inputData(Map.of(
      "students", List.of(studentFields)
    )).build();

    StudentsPreparer preparer = new StudentsPreparer();
    assertThat(preparer.prepareSubmissionFields(submission, null)).containsAllEntriesOf(Map.ofEntries(
      Map.entry("student1-first-name", new SingleField("student1-first-name", "firsty", null)),
      Map.entry("student1-last-name", new SingleField("student1-last-name", "lastnameson", null)),
      Map.entry("student1-withdrawn-school-code", new SingleField("student1-withdrawn-school-code", "", null)),
      Map.entry("student1-withdrawn-school-district", new SingleField("student1-withdrawn-school-district", "", null)),
      Map.entry("student1-withdrawn-school", new SingleField("student1-withdrawn-school", "Other In Person School", null)),
      Map.entry("student1-anticipated-school-code", new SingleField("student1-anticipated-school-code", "", null)),
      Map.entry("student1-anticipated-school-district", new SingleField("student1-anticipated-school-district", "", null)),
      Map.entry("student1-anticipated-school", new SingleField("student1-anticipated-school", "Yet another In Person School", null)),
      Map.entry("student1-virtual-school", new SingleField("student1-virtual-school", "Some Virtual Academy", null))
    ));
  }
}
