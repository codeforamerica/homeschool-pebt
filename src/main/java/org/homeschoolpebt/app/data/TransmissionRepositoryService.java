package org.homeschoolpebt.app.data;

import formflow.library.data.Submission;
import formflow.library.data.SubmissionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TransmissionRepositoryService {
  SubmissionRepository submissionRepository;
  TransmissionRepository transmissionRepository;

  public TransmissionRepositoryService(SubmissionRepository submissionRepository, TransmissionRepository transmissionRepository) {
    this.submissionRepository = submissionRepository;
    this.transmissionRepository = transmissionRepository;
  }

  public List<Submission> submissionsToTransmit() {
    return this.transmissionRepository.submissionsToTransmit(Sort.unsorted());
  }
}
