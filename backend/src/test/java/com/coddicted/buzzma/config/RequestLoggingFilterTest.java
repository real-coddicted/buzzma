package com.coddicted.buzzma.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

  private RequestLoggingFilter filter;
  private Logger logbackLogger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    filter = new RequestLoggingFilter(new ConfigProvider(Optional.empty()));
    logbackLogger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    logbackLogger.setLevel(Level.INFO);
    appender = new ListAppender<>();
    appender.start();
    logbackLogger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logbackLogger.detachAppender(appender);
  }

  @Test
  void redactsAccessAndRefreshTokensInResponseBody() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain =
        (req, res) -> {
          res.setContentType("application/json");
          res.getWriter()
              .write("{\"accessToken\":\"eyJabc.def.ghi\",\"refreshToken\":\"rtok-123\"}");
        };

    filter.doFilter(request, response, chain);

    String logged = soleExchangeMessage();
    assertThat(logged).doesNotContain("eyJabc.def.ghi").doesNotContain("rtok-123");
    assertThat(logged)
        .contains("\"accessToken\":\"***REDACTED***\"")
        .contains("\"refreshToken\":\"***REDACTED***\"");
  }

  @Test
  void redactsPasswordInRequestBody() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
    request.setContentType("application/json");
    request.setContent("{\"email\":\"a@b.com\",\"password\":\"hunter2\"}".getBytes());
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (req, res) -> req.getInputStream().readAllBytes();

    filter.doFilter(request, response, chain);

    String logged = soleExchangeMessage();
    assertThat(logged).doesNotContain("hunter2");
    assertThat(logged)
        .contains("\"password\":\"***REDACTED***\"")
        .contains("\"email\":\"a@b.com\"");
  }

  @Test
  void leavesNonSensitiveFieldsUnchanged() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/campaigns");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain =
        (req, res) -> {
          res.setContentType("application/json");
          res.getWriter().write("{\"id\":42,\"name\":\"Summer Sale\"}");
        };

    filter.doFilter(request, response, chain);

    String logged = soleExchangeMessage();
    assertThat(logged).contains("{\"id\":42,\"name\":\"Summer Sale\"}");
  }

  private String soleExchangeMessage() {
    return appender.list.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .filter(m -> m.startsWith("HTTP EXCHANGE:"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No HTTP EXCHANGE log message captured"));
  }
}
