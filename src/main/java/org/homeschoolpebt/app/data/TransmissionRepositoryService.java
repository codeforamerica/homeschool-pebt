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
    var previousTransmission = this.transmissionRepository.latestTransmission();
    var previousApplicationNumber = (previousTransmission == null) ? null : previousTransmission.getApplicationNumber();

    var transmission = Transmission.fromSubmission(submission);
    transmission.setApplicationNumber(nextApplicationNumber(previousApplicationNumber));

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
  private String nextApplicationNumber(String currentApplicationNumber) {
    var autoIncrementPortion = 1 + Integer.parseInt(
      (currentApplicationNumber == null) ? "0010000" : currentApplicationNumber.substring(0, currentApplicationNumber.length() - 2)
    );
    var randomPortion = Math.floor(Math.random() * 90 + 10);

    return "%07d%.0f".formatted(autoIncrementPortion, randomPortion);
  }
}
