package com.coddicted.buzzma.identity.dto;

import com.coddicted.buzzma.identity.entity.UserRole;
import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserBriefDto {

  UUID id;

  String name;

  UserRole role;

  @Nullable String upiId;
}
