package com.coddicted.buzzma.storage.service;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public interface StorageService {

  String store(String folder, String originalFilename, String contentType, byte[] data);

  ResponseBytes<GetObjectResponse> retrieve(String storageKey);

  void delete(String storageKey);
}
