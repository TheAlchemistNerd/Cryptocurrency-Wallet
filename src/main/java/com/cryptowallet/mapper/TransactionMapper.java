package com.cryptowallet.mapper;

import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.model.TransactionDocument;

public class TransactionMapper {
    public static TransactionDTO toDTO(TransactionDocument tx) {
        return new TransactionDTO(
            tx.getId(),
            tx.getFromAddress(),
            tx.getToAddress(),
            tx.getAmount(),
            tx.getCurrency(),
            tx.getSignature(),
            tx.getTimestamp(),
            tx.getStatus()
        );
    }
}
