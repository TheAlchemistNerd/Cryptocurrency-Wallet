package com.cryptowallet.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String userName,
        @NotBlank String password
) {}
