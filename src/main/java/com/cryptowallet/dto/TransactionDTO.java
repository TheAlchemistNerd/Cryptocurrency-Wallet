package com.cryptowallet.dto;

import java.util.Date;

public record TransactionDTO(
    String transactionId,
    String senderAddress,
    String receiverAddress,
    double amount,
    Date timestamp,
    String signature
) {}
