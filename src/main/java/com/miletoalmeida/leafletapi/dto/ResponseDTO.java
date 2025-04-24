package com.miletoalmeida.leafletapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {

    @JsonProperty("status")
    private Integer statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    @JsonProperty("path")
    private String path;

    @JsonProperty("metadata")
    private com.miletoalmeida.leafletapi.dto.ResponseMetadata metadata;

    public void setMetadata(Integer page, Integer size, Long totalElements, Integer totalPages) {
        this.metadata = com.miletoalmeida.leafletapi.dto.ResponseMetadata.of(page, size, totalElements, totalPages);
    }

    public static <T> ResponseDTO<T> success(T data) {
        return ResponseDTO.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Operação realizada com sucesso")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> success(T data, String message) {
        return ResponseDTO.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> created(T data) {
        return ResponseDTO.<T>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Recurso criado com sucesso")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> notFound(String message) {
        return ResponseDTO.<T>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> badRequest(String message) {
        return ResponseDTO.<T>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> error(HttpStatus status, String message) {
        return ResponseDTO.<T>builder()
                .statusCode(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> error(HttpStatus status, String message, List<String> errors) {
        return ResponseDTO.<T>builder()
                .statusCode(status.value())
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ResponseDTO<T> unauthorized(String message) {
        return ResponseDTO.<T>builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}