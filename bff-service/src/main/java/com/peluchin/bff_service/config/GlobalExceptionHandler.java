package com.peluchin.bff_service.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<String> handleForbidden(
            HttpClientErrorException.Forbidden ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para realizar esta operación.");
    }

}
