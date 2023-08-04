package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

  @UpdateTimestamp
  @Temporal(TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt;

  @Column(name = "flow")
  private String flow;

  @ManyToOne
  @JoinColumn(name = "submission_id")
  private Submission submission;

  @Column(name = "confirmation_number")
  private String confirmationNumber;

  @Temporal(TIMESTAMP)
  @Column(name = "submitted_to_state_at")
  private Date submittedToStateAt;

  @Column(name = "submitted_to_state_filename")
  private String submittedToStateFilename;

  public static Transmission fromSubmission(Submission submission) {
    var transmission = new Transmission();
    transmission.setSubmission(submission);
    transmission.setFlow(submission.getFlow());
    return transmission;
  }
}
