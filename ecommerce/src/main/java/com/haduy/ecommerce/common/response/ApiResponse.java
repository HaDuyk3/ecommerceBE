package com.haduy.ecommerce.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.haduy.ecommerce.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String errorCode;
    private final String message;
    private final T data;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode code) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(code.name())
                .message(code.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(code.name())
                .message(message)
                .build();
    }

}
