package com.miletoalmeida.leafletapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ScrapingException.class)
    public ResponseEntity<ResponseDTO<Object>> handleScrapingException(ScrapingException ex) {
        ResponseDTO<Object> response = new ResponseDTO<>(
                false, "Erro durante scraping: " + ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleGenericException(Exception ex) {
        ResponseDTO<Object> response = new ResponseDTO<>(
                false, "Erro interno do servidor: " + ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}