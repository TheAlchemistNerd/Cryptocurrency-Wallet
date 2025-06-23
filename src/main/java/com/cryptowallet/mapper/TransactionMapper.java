package com.cryptowallet.mapper;

import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.model.TransactionDocument;

public class TransactionMapper {
    public static TransactionDTO toDTO(TransactionDocument tx) {
        return new TransactionDTO(
            tx.getId(),
            tx.getSenderAddress(),
            tx.getReceiverAddress(),
            tx.getAmount(),
            tx.getTimestamp(),
            tx.getSignature()
        );
    }

    public static TransactionDocument toDocument(TransactionDTO dto) {
        TransactionDocument tx = new TransactionDocument();
        tx.setId(dto.transactionId());
        tx.setSenderAddress(dto.senderAddress());
        tx.setReceiverAddress(dto.receiverAddress());
        tx.setAmount(dto.amount());
        tx.setTimestamp(dto.timestamp());
        tx.setSignature(dto.signature());
        return tx;
    }
}
