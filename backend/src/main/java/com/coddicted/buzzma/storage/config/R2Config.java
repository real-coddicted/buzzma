package com.coddicted.buzzma.storage.config;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
public class R2Config {

  private final R2Properties properties;

  public R2Config(final R2Properties properties) {
    this.properties = properties;
  }

  @Bean("r2Client")
  public S3Client r2Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(this.properties.getEndpoint()))
        .region(Region.of("auto"))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    this.properties.getAccessKey(), this.properties.getSecretKey())))
        .forcePathStyle(true)
        .build();
  }
}
