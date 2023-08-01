package org.homeschoolpebt.app.preparers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import formflow.library.data.Submission;
import formflow.library.data.UserFile;
import formflow.library.pdf.SingleField;
import org.homeschoolpebt.app.data.TransmissionRepository;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesPreparerTest {
  @InjectMocks
  FilesPreparer preparer;

  @Mock
  private TransmissionRepository transmissionRepository;

  @Test
  void includesAllUploadedFiles() throws JsonProcessingException {
    var file1 = UserFile.builder()
      .fileId(UUID.randomUUID())
      .originalName("file-1.jpg")
      .createdAt(new DateTime(2023, 1, 1, 12, 0 ,0).toDate())
      .filesize(10000F)
      .build();

    var file2 = UserFile.builder()
      .fileId(UUID.randomUUID())
      .originalName("file-2.jpg")
      .createdAt(new DateTime(2023, 2, 2, 12, 0 ,0).toDate())
      .filesize(222222F)
      .build();

    var file3 = UserFile.builder()
      .fileId(UUID.randomUUID())
      .originalName("file-3.jpg")
      .createdAt(new DateTime(2023, 3, 3, 12, 0 ,0).toDate())
      .filesize(3030303F)
      .build();

    var file4 = UserFile.builder()
      .fileId(UUID.randomUUID())
      .originalName("file-4.jpg")
      .createdAt(new DateTime(2023, 4, 4, 12, 0 ,0).toDate())
      .filesize(44044044044F)
      .build();

    when(transmissionRepository.userFilesByID(anyList())).thenAnswer(invocation -> {
      var arg = (List<UUID>)invocation.getArgument(0);
      if (arg.contains(file1.getFileId())) {
        return List.of(file1);
      } else if (arg.contains(file2.getFileId())) {
        return List.of(file2);
      } else if (arg.contains(file3.getFileId())) {
        return List.of(file3);
      } else if (arg.contains(file4.getFileId())) {
        return List.of(file4);
      } else {
        return List.of();
      }
    });

    ObjectMapper json = new ObjectMapper();
    Submission submission = Submission.builder().inputData(Map.ofEntries(
      Map.entry("firstName", "Yappy"),
      Map.entry("lastName", "Yellowfruit"),
      Map.entry("identityFiles", json.writeValueAsString(List.of(file1.getFileId()))),
      Map.entry("enrollmentFiles", json.writeValueAsString(List.of(file2.getFileId()))),
      Map.entry("incomeFiles", json.writeValueAsString(List.of(file3.getFileId()))),
      Map.entry("unearnedIncomeFiles", json.writeValueAsString(List.of(file4.getFileId())))
    )).build();

    var fields = preparer.prepareSubmissionFields(submission, null);

    assertThat(fields).containsAllEntriesOf(Map.ofEntries(
      Map.entry("file1-name", new SingleField("file1-name", "file-1.jpg", null)),
      Map.entry("file1-type", new SingleField("file1-type", "Identity Verification", null)),
      Map.entry("file1-size", new SingleField("file1-size", "9.8 KB", null)),
      Map.entry("file1-submission-date", new SingleField("file1-submission-date", "1/1/2023 12:00 PM", null))
    ));

    assertThat(fields).containsAllEntriesOf(Map.ofEntries(
      Map.entry("file2-name", new SingleField("file2-name", "file-2.jpg", null)),
      Map.entry("file2-type", new SingleField("file2-type", "Virtual School Enrollment", null)),
      Map.entry("file2-size", new SingleField("file2-size", "217 KB", null)),
      Map.entry("file2-submission-date", new SingleField("file2-submission-date", "2/2/2023 12:00 PM", null))
    ));

    assertThat(fields).containsAllEntriesOf(Map.ofEntries(
      Map.entry("file3-name", new SingleField("file3-name", "file-3.jpg", null)),
      Map.entry("file3-type", new SingleField("file3-type", "Income", null)),
      Map.entry("file3-size", new SingleField("file3-size", "2.9 MB", null)),
      Map.entry("file3-submission-date", new SingleField("file3-submission-date", "3/3/2023 12:00 PM", null))
    ));

    assertThat(fields).containsAllEntriesOf(Map.ofEntries(
      Map.entry("file4-name", new SingleField("file4-name", "file-4.jpg", null)),
      Map.entry("file4-type", new SingleField("file4-type", "Income (Unearned)", null)),
      Map.entry("file4-size", new SingleField("file4-size", "41 GB", null)),
      Map.entry("file4-submission-date", new SingleField("file4-submission-date", "4/4/2023 12:00 PM", null))
    ));
  }
}
