package com.coddicted.buzzma.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Injects the authenticated user' into a controller method parameter.
 *
 * <p>Works in conjunction with {@link com.coddicted.buzzma.identity.entity.BuzzmaUser},
 *
 * <p>Use {@code @AuthenticationPrincipal(expression = "user")} internally via this meta-annotation.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "user")
public @interface CurrentUser {}
