package com.cryptowallet.dto;

import java.math.BigDecimal;
import java.util.Map;

public record WalletDTO(
        String id,
        String userId,
        String address, // This is the public key, serving as the wallet's address
        Map<String,BigDecimal> balances
) {}
