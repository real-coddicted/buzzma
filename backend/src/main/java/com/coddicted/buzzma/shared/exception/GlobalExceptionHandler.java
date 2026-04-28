package com.coddicted.buzzma.shared.exception;

import io.jsonwebtoken.JwtException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
    LOGGER.error(ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("error", ex.getCode());
    body.put("message", ex.getMessage());
    return ResponseEntity.status(ex.getStatus()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    LOGGER.error(ex.getMessage(), ex);
    Map<String, Object> errors = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      errors.put(fe.getField(), fe.getDefaultMessage());
    }
    Map<String, Object> body = new HashMap<>();
    body.put("error", "VALIDATION_FAILED");
    body.put("fields", errors);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
    LOGGER.error(ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("error", ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString());
    body.put("message", ex.getMessage());
    return ResponseEntity.status(ex.getStatusCode()).body(body);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
    LOGGER.error(ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("error", "INVALID_TOKEN");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
    LOGGER.warn(ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("error", "ACCESS_DENIED");
    body.put("message", "You do not have permission to access this resource");
    body.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler({
    MissingRequestHeaderException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
    LOGGER.warn("Bad request: {}", ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("error", "BAD_REQUEST");
    body.put("message", ex.getMessage());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    LOGGER.warn("Method not allowed: {}", ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("error", "METHOD_NOT_ALLOWED");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex) {
    LOGGER.warn("Unsupported media type: {}", ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("error", "UNSUPPORTED_MEDIA_TYPE");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<Map<String, Object>> handleNotFound(Exception ex) {
    LOGGER.warn("Not found: {}", ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("error", "NOT_FOUND");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    LOGGER.error(ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("error", "INTERNAL_SERVER_ERROR");
    body.put("message", "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
