package com.cryptowallet.dto;

import com.cryptowallet.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDTO(
    String id,
    String fromAddress,
    String toAddress,
    BigDecimal amount,
    String currency,
    String signature,
    Instant timestamp,
    TransactionStatus status
) {}
