package com.coddicted.buzzma.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Injects the admin viewing as the current user ({@link ViewAs}) into a controller method
 * parameter, or {@code null} outside view-as mode.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "actor")
public @interface ViewAsActor {}
