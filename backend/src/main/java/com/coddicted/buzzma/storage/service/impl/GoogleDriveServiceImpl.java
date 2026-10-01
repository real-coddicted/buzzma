package com.coddicted.buzzma.storage.service.impl;

import com.coddicted.buzzma.storage.service.GoogleDriveService;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.storage.google-drive.enabled", havingValue = "true")
public class GoogleDriveServiceImpl implements GoogleDriveService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveServiceImpl.class);
  private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

  private final Drive drive;

  public GoogleDriveServiceImpl(final Drive drive) {
    this.drive = drive;
  }

  @Override
  public String uploadFile(
      final String folderId, final String filename, final String contentType, final byte[] data) {
    try {
      final File fileMetadata = new File();
      fileMetadata.setName(filename);
      fileMetadata.setParents(List.of(folderId));

      final ByteArrayContent mediaContent = new ByteArrayContent(contentType, data);

      final File uploaded =
          this.drive
              .files()
              .create(fileMetadata, mediaContent)
              .setFields("id,webViewLink")
              .execute();

      final Permission publicPermission = new Permission();
      publicPermission.setType("anyone");
      publicPermission.setRole("reader");
      this.drive.permissions().create(uploaded.getId(), publicPermission).execute();

      LOGGER.debug("Uploaded file to Google Drive: id={}, name={}", uploaded.getId(), filename);
      return uploaded.getWebViewLink();
    } catch (final IOException e) {
      throw new GoogleDriveUploadException("Failed to upload file to Google Drive: " + filename, e);
    }
  }

  @Override
  public String findOrCreateFolder(final String parentId, final String folderName) {
    try {
      final String query =
          String.format(
              "mimeType='%s' and name='%s' and '%s' in parents and trashed=false",
              FOLDER_MIME_TYPE, folderName.replace("'", "\\'"), parentId);

      final List<File> existing =
          this.drive
              .files()
              .list()
              .setQ(query)
              .setFields("files(id)")
              .setPageSize(1)
              .execute()
              .getFiles();

      if (existing != null && !existing.isEmpty()) {
        return existing.get(0).getId();
      }

      final File folderMetadata = new File();
      folderMetadata.setName(folderName);
      folderMetadata.setMimeType(FOLDER_MIME_TYPE);
      folderMetadata.setParents(List.of(parentId));

      final File created = this.drive.files().create(folderMetadata).setFields("id").execute();

      LOGGER.debug("Created Google Drive folder: id={}, name={}", created.getId(), folderName);
      return created.getId();
    } catch (final IOException e) {
      throw new GoogleDriveUploadException("Failed to find or create folder: " + folderName, e);
    }
  }

  @Override
  public void deleteFile(final String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }
    try {
      final String fileId = extractFileId(fileUrl);
      if (fileId != null) {
        this.drive.files().delete(fileId).execute();
        LOGGER.debug("Deleted Google Drive file: id={}", fileId);
      }
    } catch (final IOException e) {
      LOGGER.warn("Failed to delete Google Drive file {}: {}", fileUrl, e.getMessage());
    }
  }

  private String extractFileId(final String webViewLink) {
    final int start = webViewLink.indexOf("/d/");
    if (start < 0) {
      return null;
    }
    final int end = webViewLink.indexOf("/", start + 3);
    return end < 0 ? webViewLink.substring(start + 3) : webViewLink.substring(start + 3, end);
  }

  public static class GoogleDriveUploadException extends RuntimeException {
    public GoogleDriveUploadException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}
