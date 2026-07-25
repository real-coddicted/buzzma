package com.coddicted.buzzma.configurator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BuzzmaConfiguratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(BuzzmaConfiguratorApplication.class, args);
  }
}
