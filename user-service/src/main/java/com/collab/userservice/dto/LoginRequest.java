package com.collab.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login request")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "password123")
    private String password;
}
