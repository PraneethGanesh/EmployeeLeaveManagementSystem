package com.example.EmployeeLeaveManagementSystem.Controller;

import com.example.EmployeeLeaveManagementSystem.Exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildError(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return body;
    }

    @ExceptionHandler(EmployeeNotFound.class)
    public ResponseEntity<Map<String,Object>> handelEmployeeNotFound(EmployeeNotFound exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidStartDateException.class)
    public ResponseEntity<Map<String,Object>> handelInvalidStartDateException(InvalidStartDateException exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidEndDateException.class)
    public ResponseEntity<Map<String,Object>> handelInvalidEndDateException(InvalidEndDateException exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(OverlappingLeaveException.class)
    public ResponseEntity<Map<String,Object>> handelOverlappingLeaveException(OverlappingLeaveException exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<Map<String,Object>> handelDuplicateRequestException(DuplicateRequestException exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handelLeaveRequestNotFoundException(LeaveRequestNotFoundException exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidManagerException.class)
    public ResponseEntity<Map<String,Object>> handelLeaveInvalidManagerException(InvalidManagerException exception, WebRequest request){
        return new ResponseEntity<>(
                buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }



}
