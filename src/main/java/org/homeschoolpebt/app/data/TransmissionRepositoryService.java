package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TransmissionRepositoryService {
  @Autowired
  SubmissionRepository submissionRepository;

  @Autowired
  TransmissionRepository transmissionRepository;

  public Transmission createTransmissionRecord(Submission submission) {
    var transmission = Transmission.fromSubmission(submission);

    // Set an application number and check that it hasn't been already used

    this.transmissionRepository.save(transmission);

    return transmission;
  }
}
