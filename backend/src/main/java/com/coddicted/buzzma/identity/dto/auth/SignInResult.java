package com.coddicted.buzzma.identity.dto.auth;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;

public record SignInResult(BuzzmaUser user, TokensDto tokens) {}
