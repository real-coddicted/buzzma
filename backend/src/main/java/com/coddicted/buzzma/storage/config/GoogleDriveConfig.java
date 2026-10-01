package com.coddicted.buzzma.storage.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.storage.google-drive.enabled", havingValue = "true")
public class GoogleDriveConfig {

  private final GoogleDriveProperties properties;

  public GoogleDriveConfig(final GoogleDriveProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Drive googleDriveService() throws GeneralSecurityException, IOException {
    final GoogleCredentials credentials =
        GoogleCredentials.fromStream(new FileInputStream(this.properties.getCredentialsPath()))
            .createScoped(List.of(DriveScopes.DRIVE));

    return new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
        .setApplicationName("Buzzma")
        .build();
  }
}
