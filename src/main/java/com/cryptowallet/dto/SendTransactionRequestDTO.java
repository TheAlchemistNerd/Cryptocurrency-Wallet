package com.cryptowallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SendTransactionRequestDTO (
    @NotBlank(message = "senderWalletId must not be blank")
    String senderWalletId,
    @NotBlank(message = "receiverAddress must not be blank")
    String receiverAddress,
    @Positive(message = "amount must be positive")
    double amount
){
    public String generateIdempotencyKey() {
       return senderWalletId + ":" + receiverAddress + ":" + amount;
    }
}
