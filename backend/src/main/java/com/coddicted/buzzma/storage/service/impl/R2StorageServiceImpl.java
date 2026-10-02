package com.coddicted.buzzma.storage.service.impl;

import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.config.R2Properties;
import com.coddicted.buzzma.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Public mirror of screenshots on Cloudflare R2. Unlike the primary store, {@link #store} keeps the
 * filename as given, so storing the same folder and filename again overwrites the object.
 */
@Service("r2StorageService")
@ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
public class R2StorageServiceImpl implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(R2StorageServiceImpl.class);

  private final S3Client r2Client;
  private final String bucket;

  public R2StorageServiceImpl(
      @Qualifier("r2Client") final S3Client r2Client, final R2Properties properties) {
    this.r2Client = r2Client;
    this.bucket = properties.getBucket();
  }

  @Override
  public String store(
      final String folder,
      final String originalFilename,
      final String contentType,
      final byte[] data) {
    final String storageKey = folder + "/" + originalFilename;
    final PutObjectRequest putRequest =
        PutObjectRequest.builder()
            .bucket(this.bucket)
            .key(storageKey)
            .contentType(contentType)
            .build();
    this.r2Client.putObject(putRequest, RequestBody.fromBytes(data));
    LOGGER.debug("Stored file in R2: bucket={}, key={}", this.bucket, storageKey);
    return storageKey;
  }

  @Override
  public ResponseBytes<GetObjectResponse> retrieve(final String storageKey) {
    try {
      return this.r2Client.getObject(
          GetObjectRequest.builder().bucket(this.bucket).key(storageKey).build(),
          ResponseTransformer.toBytes());
    } catch (final NoSuchKeyException e) {
      throw new NotFoundException("File not found: " + storageKey);
    }
  }

  @Override
  public void delete(final String storageKey) {
    try {
      this.r2Client.deleteObject(
          DeleteObjectRequest.builder().bucket(this.bucket).key(storageKey).build());
      LOGGER.debug("Deleted file from R2: bucket={}, key={}", this.bucket, storageKey);
    } catch (final Exception e) {
      LOGGER.warn("Failed to delete R2 file {}: {}", storageKey, e.getMessage());
    }
  }
}
