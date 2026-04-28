package com.coddicted.buzzma.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Injects the authenticated user's UUID into a controller method parameter.
 *
 * <p>Works in conjunction with {@link MoboUserDetails}: the principal's {@code getUsername()}
 * returns the UUID string which is parsed into {@link java.util.UUID}.
 *
 * <p>Use {@code @AuthenticationPrincipal(expression = "user.id")} internally via this
 * meta-annotation.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "user.id")
public @interface CurrentUserId {}
