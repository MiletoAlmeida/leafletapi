package com.miletoalmeida.leafletapi.util;

import com.miletoalmeida.leafletapi.dto.ResponseDTO;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@UtilityClass
public class ResponseBuilder {

    public static <T> ResponseEntity<ResponseDTO<T>> ok(T data) {
        ResponseDTO<T> response = ResponseDTO.success(data);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> ok(T data, String message) {
        ResponseDTO<T> response = ResponseDTO.success(data, message);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ResponseDTO<List<T>>> ok(Page<T> page) {
        ResponseDTO<List<T>> response = ResponseDTO.success(page.getContent());
        response.setMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> created(T data) {
        ResponseDTO<T> response = ResponseDTO.created(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> notFound(String message) {
        ResponseDTO<T> response = ResponseDTO.notFound(message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> badRequest(String message) {
        ResponseDTO<T> response = ResponseDTO.badRequest(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> error(HttpStatus status, String message) {
        ResponseDTO<T> response = ResponseDTO.error(status, message);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> error(
            HttpStatus status, 
            String message, 
            List<String> errors
    ) {
        ResponseDTO<T> response = ResponseDTO.error(status, message, errors);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> unauthorized(String message) {
        ResponseDTO<T> response = ResponseDTO.unauthorized(message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}