package com.cryptowallet.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDTO(
    String id,
    String fromAddress,
    String toAddress,
    BigDecimal amount,
    String currency,
    String signature,
    Instant timestamp
) {}
