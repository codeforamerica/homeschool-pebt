package org.homeschoolpebt.app.cli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class TransmitterCommands {

  @ShellMethod(key = "hello")
  public String helloWorld(
    @ShellOption(defaultValue = "world") String name
  ) {
    return "hello " + name;
  }
}
