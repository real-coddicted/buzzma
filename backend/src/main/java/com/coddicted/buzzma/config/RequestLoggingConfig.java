package com.coddicted.buzzma.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RequestLoggingConfig {

  @Bean
  public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(
      RequestLoggingFilter requestLoggingFilter) {
    FilterRegistrationBean<RequestLoggingFilter> registration =
        new FilterRegistrationBean<>(requestLoggingFilter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.addUrlPatterns("/*");
    return registration;
  }
}
