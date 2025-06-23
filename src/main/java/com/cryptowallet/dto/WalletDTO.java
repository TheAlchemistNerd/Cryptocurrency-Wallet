package com.cryptowallet.dto;

public record WalletDTO(
    String walletId,
    String userId,
    String publicKey,
    double balance
) {}
