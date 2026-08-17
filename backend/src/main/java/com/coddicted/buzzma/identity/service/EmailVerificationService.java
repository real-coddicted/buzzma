package com.coddicted.buzzma.identity.service;

import java.util.UUID;

public interface EmailVerificationService {

  void sendOtp(UUID userId);

  void verifyOtp(UUID userId, String code);
}
