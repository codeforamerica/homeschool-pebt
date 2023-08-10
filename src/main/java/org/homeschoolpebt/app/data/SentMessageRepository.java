package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SentMessageRepository extends JpaRepository<SentMessage, UUID> {
  @Query("SELECT sm FROM SentMessage sm WHERE sm.submission IN :submissions AND sm.messageName = :messageName AND sm.sentAt IS NOT NULL")
  List<SentMessage> findAllBySubmissionsAndMessageName(@Param("submissions") List<Submission> submissions, @Param("messageName") String messageName);
}
