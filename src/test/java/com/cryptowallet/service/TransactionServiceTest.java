package com.cryptowallet.service;

import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.repository.TransactionRepository;
import com.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private WalletRepository walletRepo;
    private TransactionRepository txRepo;
    private MongoTemplate mongoTemplate;
    private TransactionService transactionService;

    @BeforeEach
    void setup() {
        walletRepo = mock(WalletRepository.class);
        txRepo = mock(TransactionRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        transactionService = new TransactionService(walletRepo, txRepo, mongoTemplate);
    }


    @Test
    void testIdempotentTransaction() {
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO("sender123", "recv456", 100);
        TransactionDocument existing = new TransactionDocument("sender123", "recv456", 100, null, dto.generateIdempotencyKey());

        when(txRepo.findBySenderAddress("sender123")).thenReturn(Collections.singletonList(existing));

        var result = transactionService.processTransaction(dto);
        assertEquals(existing.getReceiverAddress(), result.receiverAddress());

        verifyNoInteractions(mongoTemplate);
    }


}
