package com.saborclick.auth.common.exception;

import com.saborclick.auth.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class DeserializationErrorHandler {

    @ExceptionHandler(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class)
    public ResponseEntity<ErrorResponse> handleUnknownFields(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException ex) {
        String traceId = MDC.get("traceId");
        String fieldName = ex.getPropertyName();

        String message = "🚫 Campo no permitido en el JSON: '" + fieldName + "'";
        if (traceId != null) {
            message += " | Trace ID: " + traceId;
        }

        log.warn("Campo desconocido recibido: {}", fieldName);

        ErrorResponse error = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
