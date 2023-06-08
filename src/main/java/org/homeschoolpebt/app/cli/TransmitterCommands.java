package org.homeschoolpebt.app.cli;

import org.homeschoolpebt.app.data.TransmissionRepository;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TransmitterCommands {

  @Autowired
  TransmissionRepositoryService transmissionRepositoryService;

  @Autowired
  TransmissionRepository transmissionRepository;

  @ShellMethod(key = "transmit")
  public void transmit() {
    System.out.println("Submissions to transmit....");

    this.transmissionRepository.submissionsToTransmit(Sort.unsorted()).forEach(i -> {
      System.out.println(i.toString());
    });
  }
}
