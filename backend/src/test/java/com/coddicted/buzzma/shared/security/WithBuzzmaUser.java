package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.identity.entity.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithBuzzmaUserSecurityContextFactory.class)
public @interface WithBuzzmaUser {
  UserRole role();

  String id() default "";
}
