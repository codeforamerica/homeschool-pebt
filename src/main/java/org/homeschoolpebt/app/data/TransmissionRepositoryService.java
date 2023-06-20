package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class TransmissionRepositoryService {
  @Autowired
  SubmissionRepository submissionRepository;

  @Autowired
  TransmissionRepository transmissionRepository;

  public Transmission createTransmissionRecord(Submission submission) {
    if (!submission.getFlow().equals("pebt")) {
      throw new RuntimeException("Non-Pebt object passed to createTransmissionRecord");
    }

    var previousTransmission = this.transmissionRepository.latestApplicationTransmission();
    var previousApplicationNumber = (previousTransmission == null) ? null : previousTransmission.getConfirmationNumber();

    var transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber(nextApplicationConfirmationNumber(previousApplicationNumber));

    this.transmissionRepository.save(transmission);

    return transmission;
  }

  public Transmission createLaterdocTransmissionRecord(Submission submission) {
    if (!submission.getFlow().equals("docUpload")) {
      throw new RuntimeException("Non-LaterDoc object passed to createTransmissionRecord");
    }

    var previousTransmission = this.transmissionRepository.latestLaterdocTransmission();
    var previousConfirmationNumber = (previousTransmission == null) ? null : previousTransmission.getConfirmationNumber();

    var transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber(nextLaterdocConfirmationNumber(previousConfirmationNumber));
    this.transmissionRepository.save(transmission);

    return transmission;
  }

  public UUID save(Transmission transmission) {
    return transmissionRepository.save(transmission).getId();
  }

  /*
    Autoincrement starting at 10,000 followed by 2 random digits, with leading
    zeroes up to 9 digits (when stored in the database only.)

    e.g. 001000142 - a possible first application number (will be presented to client as 1000142)
    e.g. 002523469 - a possible 15,234th application number (will be presented to client as 2523469)
  */
  private String nextApplicationConfirmationNumber(String currentConfirmationNumber) {
    var autoIncrementPortion = 1 + Integer.parseInt(
      (currentConfirmationNumber == null) ? "0010000" : currentConfirmationNumber.substring(0, currentConfirmationNumber.length() - 2)
    );
    var randomPortion = Math.floor(Math.random() * 90 + 10);

    return "%07d%.0f".formatted(autoIncrementPortion, randomPortion);
  }

  /*
    Laterdocs have the same format as application confirmation numbers except they
    end with an L.
   */
  private String nextLaterdocConfirmationNumber(String currentConfirmationNumber) {
    var autoIncrementPortion = 1 + Integer.parseInt(
      (currentConfirmationNumber == null) ? "0010000" : currentConfirmationNumber.substring(0, currentConfirmationNumber.length() - 3)
    );
    var randomPortion = Math.floor(Math.random() * 90 + 10);

    return "%07d%.0fL".formatted(autoIncrementPortion, randomPortion);
  }
}
