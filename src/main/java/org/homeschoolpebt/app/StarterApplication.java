package org.homeschoolpebt.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"org.homeschoolpebt.app", "formflow.library"})
@EnableConfigurationProperties
public class StarterApplication {

  public static void main(String[] args) {

    SpringApplication.run(StarterApplication.class, args);
  }

}
