package com.cryptowallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDTO(
    @NotBlank @Size(min = 3, message = "Username must at least be 3 characters") String userName,
    @NotBlank @Email(message = "A valid email is required") String email,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long") String password
) {}
