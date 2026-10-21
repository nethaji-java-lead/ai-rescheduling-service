package com.carebridge.exception;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ResourceNotFoundException.class)
 ResponseEntity<Map<String,Object>> notFound(RuntimeException ex) { return response(HttpStatus.NOT_FOUND, ex.getMessage()); }
 @ExceptionHandler({ConflictException.class, IllegalStateException.class})
 ResponseEntity<Map<String,Object>> conflict(RuntimeException ex) { return response(HttpStatus.CONFLICT, ex.getMessage()); }
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException ex) {
  String message = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField()+": "+e.getDefaultMessage()).findFirst().orElse("Invalid request");
  return response(HttpStatus.BAD_REQUEST, message);
 }
 @ExceptionHandler(IllegalArgumentException.class)
 ResponseEntity<Map<String,Object>> badRequest(RuntimeException ex) { return response(HttpStatus.BAD_REQUEST, ex.getMessage()); }
 private ResponseEntity<Map<String,Object>> response(HttpStatus status, String message) {
  return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error", status.getReasonPhrase(), "message", Objects.toString(message, "Request failed")));
 }
}
