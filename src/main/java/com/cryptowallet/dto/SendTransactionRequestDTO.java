package com.cryptowallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SendTransactionRequestDTO (
    @NotBlank(message = "fromAddress must not be blank")
    String fromAddress,
    @NotBlank(message = "toAddress must not be blank")
    String toAddress,
    @NotNull @DecimalMin(value = "0.00000001", message = "Amount must be positive")
    BigDecimal amount,
    @NotBlank(message = "Signature is required for the transaction") String signature,
    @NotBlank(message = "Currency must be specified") String currency
){
    /**
     * Creates a standardized string representation of the transaction details for signing.
     * The client should use this exact format to generate the signature.
     * @return A string to be signed, e.g., "fromAddress:toAddress:amount:currency"
     */
    public String toSignableString() {
        return String.join(":", fromAddress, toAddress, amount.toPlainString(), currency);
    }
}
