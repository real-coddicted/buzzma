package com.coddicted.buzzma.storage.service;

public interface GoogleDriveService {

  String uploadFile(String folderId, String filename, String contentType, byte[] data);

  String findOrCreateFolder(String parentId, String folderName);

  void deleteFile(String fileUrl);
}
