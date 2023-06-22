package org.homeschoolpebt.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.homeschoolpebt.app", "formflow.library"})
@EntityScan(basePackages = {"org.homeschoolpebt.app", "formflow.library"})
@EnableConfigurationProperties
@EnableScheduling
public class StarterApplication {

  public static void main(String[] args) {

    SpringApplication.run(StarterApplication.class, args);
  }

}
