package com.coddicted.buzzma.shared.storage;

public interface StorageService {

  String store(String folder, String originalFilename, String contentType, byte[] data);

  byte[] retrieve(String storageKey);

  void delete(String storageKey);
}
