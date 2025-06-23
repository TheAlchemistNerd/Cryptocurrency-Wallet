package com.cryptowallet.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletRequestDTO(
    @NotBlank(message = "userId must not be blank")
    String userId
) {}
