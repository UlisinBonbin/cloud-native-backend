package com.peluchin.admin_service.exception;

import com.peluchin.admin_service.dto.MensajeRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el microservicio administrador.
 * Garantiza respuestas de error claras y estructuradas (Indicador 8).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeRespuesta> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });

        MensajeRespuesta respuesta = MensajeRespuesta.error(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los parámetros de entrada",
                errores
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MensajeRespuesta> handleIllegalArgumentException(IllegalArgumentException ex) {
        MensajeRespuesta respuesta = MensajeRespuesta.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeRespuesta> handleGeneralException(Exception ex) {
        MensajeRespuesta respuesta = MensajeRespuesta.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno al procesar la solicitud: " + ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}
