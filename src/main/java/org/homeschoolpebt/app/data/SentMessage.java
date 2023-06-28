package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "sent_messages")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SentMessage {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "submission_id")
  private Submission submission;

  @Column(name = "message_name")
  private String messageName;

  @CreationTimestamp
  @Temporal(TIMESTAMP)
  @Column(name = "sent_at")
  private Date sentAt;

  @Column(name = "sent_status")
  private String sentStatus;

  @Column(name = "provider")
  private String provider;

  @Column(name = "provider_message_id")
  private String providerMessageId;
}
