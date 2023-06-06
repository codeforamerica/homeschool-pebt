package org.homeschoolpebt.app.cli;

import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TransmitterCommands {

  @Autowired
  TransmissionRepositoryService transmissionRepositoryService;

  @ShellMethod(key = "transmit")
  public void transmit() {
    System.out.println("Submissions to transmit....");

    this.transmissionRepositoryService.submissionsToTransmit().forEach(i -> {
      System.out.println(i.toString());
    });
  }
}
