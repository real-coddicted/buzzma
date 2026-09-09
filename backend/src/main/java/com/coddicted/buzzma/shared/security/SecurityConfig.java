package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.security.JwtAuthenticationFilter;
import com.coddicted.buzzma.shared.ratelimit.AuthBucketCache;
import com.coddicted.buzzma.shared.ratelimit.RateLimitFilter;
import com.coddicted.buzzma.shared.ratelimit.UserBucketCache;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthBucketCache authBucketCache;
  private final UserBucketCache userBucketCache;
  private final IpAddressMatcher metricsNetworkMatcher;

  public SecurityConfig(
      final JwtAuthenticationFilter jwtAuthenticationFilter,
      final AuthBucketCache authBucketCache,
      final UserBucketCache userBucketCache,
      @Value("${docker.network.cidr:127.0.0.1/32}") final String dockerNetworkCidr) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.authBucketCache = authBucketCache;
    this.userBucketCache = userBucketCache;
    this.metricsNetworkMatcher = new IpAddressMatcher(dockerNetworkCidr);
  }

  @Bean
  public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .access(this::isFromMetricsNetwork)
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/api/health/**",
                        "/api/v1/security-questions",
                        "/api/v1/terms",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(
            new RateLimitFilter(authBucketCache, userBucketCache), JwtAuthenticationFilter.class);
    return http.build();
  }

  private AuthorizationDecision isFromMetricsNetwork(
      final Supplier<Authentication> authentication, final RequestAuthorizationContext context) {
    return new AuthorizationDecision(this.metricsNetworkMatcher.matches(context.getRequest()));
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy(
        UserRole.ROLE_ADMIN.name()
            + " > "
            + UserRole.ROLE_BUYER.name()
            + "\n"
            + UserRole.ROLE_ADMIN.name()
            + " > "
            + UserRole.ROLE_MEDIATOR.name()
            + "\n"
            + UserRole.ROLE_ADMIN.name()
            + " > "
            + UserRole.ROLE_AGENCY.name()
            + "\n"
            + UserRole.ROLE_ADMIN.name()
            + " > "
            + UserRole.ROLE_BRAND.name());
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      final RoleHierarchy roleHierarchy) {
    final DefaultMethodSecurityExpressionHandler handler =
        new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Content-Disposition"));
    config.setAllowCredentials(true);

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
