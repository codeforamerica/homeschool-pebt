package org.homeschoolpebt.app.preparers;

import formflow.library.data.Submission;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.utils.SchoolListUtilities;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StudentsPreparer implements SubmissionFieldPreparer {
  public static final Pattern OFFICAL_SCHOOL_FORMAT = Pattern.compile("\\A(?<cdsCode>\\d{14}) - (?<school>.*) \\((?<district>.*)\\)\\Z", Pattern.CASE_INSENSITIVE);

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();

    var students = submission.getInputData().get("students");
    if (students == null) {
      return Map.of();
    }

    var studentIndex = 1;
    for (var student : (List<Map<String, Object>>) students) {
      var studentFields = studentFields(student);
      for (var entry : studentFields.entrySet()) {
        // e.g. student1-first-name
        var fieldName = "student%s-%s".formatted(studentIndex, entry.getKey());
        var fieldValue = (String) entry.getValue();

        fields.put(
          fieldName,
          new SingleField(fieldName, fieldValue, null)
        );
      }

      studentIndex += 1;
    }

    return fields;
  }

  private HashMap<String, String> studentFields(Map<String, Object> student) {
    var fields = new HashMap<String, String>();

    var anticipated = parseSchoolName((String) student.get("studentWouldAttendSchoolName"));
    var anticipatedCep = SchoolListUtilities.allCepSchools(List.of(anticipated.get(0)));
    fields.put("anticipated-school-code", anticipated.get(0));
    fields.put("anticipated-school-district", anticipated.get(1));
    fields.put("anticipated-school", anticipated.get(2));
    fields.put("anticipated-school-cep", anticipatedCep ? "Yes" : "No");

    var studentUnenrolledSchoolName = (String) student.get("studentUnenrolledSchoolName");
    if (studentUnenrolledSchoolName != null) {
      var withdrawn = parseSchoolName(studentUnenrolledSchoolName);
      var withdrawnCep = SchoolListUtilities.allCepSchools(List.of(withdrawn.get(0)));
      fields.put("withdrawn-school-code", withdrawn.get(0));
      fields.put("withdrawn-school-district", withdrawn.get(1));
      fields.put("withdrawn-school", withdrawn.get(2));
      fields.put("withdrawn-school-cep", withdrawnCep ? "Yes" : "No");
    }

    fields.put("virtual-school", student.getOrDefault("studentVirtualSchoolName", "").toString());

    fields.put("first-name", (String) student.get("studentFirstName"));
    fields.put("last-name", (String) student.get("studentLastName"));
    fields.put("middle-initial", (String) student.get("studentMiddleInitial"));
    fields.put("homeschool", (String) student.get("studentHomeschoolAffidavitNumber"));
    fields.put("grade", (String) student.get("studentGrade"));
    fields.put("dob", (String) student.get("studentBirthdayDate"));
    fields.put("foster", designationsIncludes(student, "foster") ? "Yes" : "No");
    fields.put("migrant", designationsIncludes(student, "migrant") ? "Yes" : "No");
    fields.put("runaway", designationsIncludes(student, "runaway") ? "Yes" : "No");
    fields.put("unhoused", designationsIncludes(student, "unhoused") ? "Yes" : "No");

    return fields;
  }

  // TODO: Move this to some Utilities class and make it return a struct?
  public static List<String> parseSchoolName(String schoolName) {
    // e.g.
    // 37680230135277 - Muraoka (Saburo) Elementary (Chula Vista Elementary)
    // |-- cdsCode -|   |-- school ---------------|  |-- district --------|
    Matcher match = OFFICAL_SCHOOL_FORMAT.matcher(schoolName);
    if (match.matches()) {
      return List.of(match.group("cdsCode"), match.group("district"), match.group("school"));
    } else {
      return List.of("", "", schoolName);
    }
  }

  private boolean designationsIncludes(Map<String, Object> student, String target) {
    var designations = (List<String>) student.getOrDefault("studentDesignations[]", new ArrayList<String>());
    return designations.contains(target);
  }
}
