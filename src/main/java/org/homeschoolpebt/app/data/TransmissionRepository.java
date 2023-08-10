package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import formflow.library.data.UserFile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransmissionRepository extends JpaRepository<Transmission, UUID> {

  @Query(value = "SELECT s FROM Submission s JOIN Transmission t ON t.submission = s WHERE s.submittedAt IS NOT NULL AND t.submittedToStateAt IS NULL ORDER BY s.updatedAt ASC ")
  List<Submission> submissionsToTransmit(Sort sort);

  @Query(value = "SELECT t FROM Transmission t WHERE t.flow = 'pebt' ORDER BY t.confirmationNumber DESC LIMIT 1")
  Transmission latestApplicationTransmission();

  @Query(value = "SELECT t FROM Transmission t WHERE t.flow = 'docUpload' ORDER BY t.confirmationNumber DESC LIMIT 1")
  Transmission latestLaterdocTransmission();

  @Query(value = "SELECT t FROM Transmission t WHERE t.submission = :submission")
  Transmission getTransmissionBySubmission(Submission submission);

  @Query(value = "SELECT u FROM UserFile u WHERE u.submission_id = :submission ORDER BY u.createdAt")
  List<UserFile> userFilesBySubmission(Submission submission);

  @Query(value = "SELECT u FROM UserFile u WHERE u.file_id IN :ids")
  List<UserFile> userFilesByID(@Param("ids") List<UUID> ids);

  // These should be in SubmissionRepository but can't because it's in the FFL:
  @Query(value = "SELECT s FROM Submission s " +
    "WHERE s.submittedAt IS NULL " +
    "AND jsonb_extract_path_text(s.inputData, 'email') IS NOT NULL")
  List<Submission> submissionsUnsubmittedWithEmail();

  @Query(value = "SELECT s FROM Submission s " +
    "WHERE s.submittedAt IS NOT NULL " +
    "AND jsonb_extract_path_text(s.inputData, 'email') IS NOT NULL")
  List<Submission> submissionsSubmittedWithEmail();
}
