package com.coddicted.buzzma.notifications.config;

import com.coddicted.buzzma.notifications.security.JwtReactiveAuthenticationManager;
import com.coddicted.buzzma.notifications.security.JwtServerAuthenticationConverter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
      final ServerHttpSecurity http,
      final JwtReactiveAuthenticationManager authenticationManager,
      final JwtServerAuthenticationConverter authenticationConverter) {

    final AuthenticationWebFilter authFilter = new AuthenticationWebFilter(authenticationManager);
    authFilter.setServerAuthenticationConverter(authenticationConverter);
    authFilter.setAuthenticationFailureHandler(
        new ServerAuthenticationEntryPointFailureHandler(
            new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));

    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .logout(logout -> logout.disable())
        .authorizeExchange(
            ex ->
                ex.pathMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .pathMatchers("/actuator/health/**", "/actuator/info")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
