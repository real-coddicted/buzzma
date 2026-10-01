package com.coddicted.buzzma.storage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class GoogleDriveServiceImplTest {

  @Test
  void extractFileId_standardWebViewLink() throws Exception {
    final String url = "https://drive.google.com/file/d/1AbCdEfGhIjKlMnOpQrStUvWxYz/view";
    final java.lang.reflect.Method method =
        GoogleDriveServiceImpl.class.getDeclaredMethod("extractFileId", String.class);
    method.setAccessible(true);
    final GoogleDriveServiceImpl service = new GoogleDriveServiceImpl(null);
    final String result = (String) method.invoke(service, url);
    assertEquals("1AbCdEfGhIjKlMnOpQrStUvWxYz", result);
  }

  @Test
  void extractFileId_noFileIdInUrl() throws Exception {
    final String url = "https://drive.google.com/folders/something";
    final java.lang.reflect.Method method =
        GoogleDriveServiceImpl.class.getDeclaredMethod("extractFileId", String.class);
    method.setAccessible(true);
    final GoogleDriveServiceImpl service = new GoogleDriveServiceImpl(null);
    final String result = (String) method.invoke(service, url);
    assertNull(result);
  }
}
