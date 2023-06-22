package org.homeschoolpebt.app.preparers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import formflow.library.data.Submission;
import formflow.library.data.UserFile;
import formflow.library.pdf.PdfMap;
import formflow.library.pdf.SingleField;
import formflow.library.pdf.SubmissionField;
import formflow.library.pdf.SubmissionFieldPreparer;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FilesPreparer implements SubmissionFieldPreparer {
  @Autowired
  TransmissionRepository transmissionRepository;

  @Override
  public Map<String, SubmissionField> prepareSubmissionFields(Submission submission, PdfMap pdfMap) {
    var fields = new HashMap<String, SubmissionField>();

    var fileIndex = 1;

    // Identity
    var identityFiles = filesOfType(submission, "identityFiles");
    for (var file : identityFiles) {
      addFile(fields, file.getOriginalName(), file.getCreatedAt(), "Identity Verification", file.getFilesize(), fileIndex);
      fileIndex += 1;
    }
    // Enrollment
    var enrollmentFiles = filesOfType(submission, "enrollmentFiles");
    for (var file : enrollmentFiles) {
      addFile(fields, file.getOriginalName(), file.getCreatedAt(), "Virtual School Enrollment", file.getFilesize(), fileIndex);
      fileIndex += 1;
    }
    // Income
    var incomeFiles = filesOfType(submission, "incomeFiles");
    for (var file : incomeFiles) {
      addFile(fields, file.getOriginalName(), file.getCreatedAt(), "Income", file.getFilesize(), fileIndex);
      fileIndex += 1;
    }
    // Unearned Income
    var unearnedIncomeFiles = filesOfType(submission, "unearnedIncomeFiles");
    for (var file : unearnedIncomeFiles) {
      addFile(fields, file.getOriginalName(), file.getCreatedAt(), "Income (Unearned)", file.getFilesize(), fileIndex);
      fileIndex += 1;
    }

    return fields;
  }

  private void addFile(Map<String, SubmissionField> fields, String name, Date submissionDate, String type, Float size, Integer i) {
    setField(fields, "file%s-name".formatted(i), name);
    setField(fields, "file%s-type".formatted(i), type);
    setField(fields, "file%s-submission-date".formatted(i), formatSubmittedAt(submissionDate));
    setField(fields, "file%s-size".formatted(i), formatFilesize(size));
  }

  private void setField(Map<String, SubmissionField> fields, String key, String value) {
    fields.put(key, new SingleField(key, value, null));
  }

  private List<UserFile> filesOfType(Submission submission, String type) {
    var data = submission.getInputData();

    ObjectMapper fromJson = new ObjectMapper();
    try {
      var uuids = fromJson.readValue((String) data.getOrDefault(type, "[]"), UUID[].class);
      return transmissionRepository.userFilesByID(List.of(uuids));
    } catch (JsonProcessingException e) {
      return List.of();
    }
  }

  // https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
  private String formatFilesize(Float bytes) {
    if(bytes <= 0) return "0";
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(bytes)/Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(bytes/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  private String formatSubmittedAt(Date submittedAt) {
    String pattern = "M/d/y h:mm a";
    SimpleDateFormat formatDate = new SimpleDateFormat(pattern);
    return formatDate.format(submittedAt);
  }
}
