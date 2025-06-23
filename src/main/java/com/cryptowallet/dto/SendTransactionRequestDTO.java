package com.cryptowallet.dto;

public record SendTransactionRequestDTO (
    String senderWalletId,
    String receiverAddress,
    double amount
){}
