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
@ConditionalOnProperty(name = "app.storage.type", havingValue = "garage")
public class StorageConfig {

  private final S3StorageProperties properties;

  public StorageConfig(final S3StorageProperties properties) {
    this.properties = properties;
  }

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(this.properties.getEndpoint()))
        .region(Region.of(this.properties.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    this.properties.getAccessKey(), this.properties.getSecretKey())))
        .forcePathStyle(true)
        .build();
  }

  @Bean("bucketName")
  public String bucket() {
    return this.properties.getBucket();
  }
}
