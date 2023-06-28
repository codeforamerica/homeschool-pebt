package org.homeschoolpebt.app.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SentMessageRepository extends JpaRepository<SentMessage, UUID> {
}
