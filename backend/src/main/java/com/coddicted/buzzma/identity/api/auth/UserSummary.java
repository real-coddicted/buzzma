package com.coddicted.buzzma.identity.api.auth;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class UserSummary {

    UUID id;

    String name;

    String mobile;

    String email;

    UserRole role;

    UserStatus status;

    String avatar;

    Instant createdAt;

    String createdBy;

    Instant updatedAt;

    String updatedBy;
}
