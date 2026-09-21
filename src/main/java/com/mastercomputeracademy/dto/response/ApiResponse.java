package com.mastercomputeracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified envelope for all API responses.
 *
 * Success:  { "success": true,  "message": "...", "data": {...},  "timestamp": "..." }
 * Error:    { "success": false, "message": "...", "errors": {...}, "timestamp": "..." }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope")
public class ApiResponse<T> {

    @Schema(description = "Whether the request succeeded", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message", example = "Certificate found")
    private String message;

    @Schema(description = "Response payload (present on success)")
    private T data;

    @Schema(description = "Validation error details (present on failure)")
    private Object errors;

    @Builder.Default
    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    // ------------------------------------------------------------------
    // Convenience factory methods
    // ------------------------------------------------------------------

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

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
