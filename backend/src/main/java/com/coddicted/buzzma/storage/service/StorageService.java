package com.coddicted.buzzma.storage.service;

public interface StorageService {

  String store(String folder, String originalFilename, String contentType, byte[] data);

  byte[] retrieve(String storageKey);

  void delete(String storageKey);
}
