package com.coddicted.buzzma.configurator.exception;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  record ErrorResponse(int status, String message) {}

  @ExceptionHandler(ConfigEntryNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(ConfigEntryNotFoundException ex) {
    return new ErrorResponse(404, ex.getMessage());
  }

  @ExceptionHandler(ConfigEntryConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleConflict(ConfigEntryConflictException ex) {
    return new ErrorResponse(409, ex.getMessage());
  }

  @ExceptionHandler(DuplicateConfigEntryException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleDuplicate(DuplicateConfigEntryException ex) {
    return new ErrorResponse(409, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
    final String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
    return new ErrorResponse(400, message);
  }
}
