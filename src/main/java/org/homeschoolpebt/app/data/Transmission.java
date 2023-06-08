package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Entity
@Data
@Table(name = "transmissions")
@Component
public class Transmission {
  @Id
  @GeneratedValue
  private UUID id;

  @CreationTimestamp
  @Temporal(TIMESTAMP)
  @Column(name = "created_at")
  private Date createdAt;

  @ManyToOne
  @JoinColumn(name = "submission_id")
  private Submission submission;

  @Column(name = "application_number")
  private String applicationNumber;

  @Temporal(TIMESTAMP)
  @Column(name = "submitted_to_state_at")
  private Date submittedToStateAt;

  @Column(name = "submitted_to_state_filename")
  private String submittedToStateFilename;

  static Transmission fromSubmission(Submission submission) {
    var transmission = new Transmission();
    transmission.setSubmission(submission);
    return transmission;
  }
}
