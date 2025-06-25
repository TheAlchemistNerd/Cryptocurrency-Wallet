package com.cryptowallet.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequestDTO(
    @NotBlank String userName,
    @NotBlank String password
) {}
