package com.mastercomputeracademy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin login credentials")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "Admin email address", example = "admin@mastercomputeracademy.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Admin password", example = "SecurePassword@123")
    private String password;
}
