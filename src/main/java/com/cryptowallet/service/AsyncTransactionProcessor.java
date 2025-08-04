package com.cryptowallet.service;

import com.cryptowallet.domain.TransactionStatus;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncTransactionProcessor {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Async
    public void processTransaction(String transactionId) {
        log.info("Asynchronously processing transaction {}", transactionId);
        transactionRepository.findById(transactionId).ifPresent(transaction -> {
            if (transaction.getStatus() == TransactionStatus.PENDING) {
                try {
                    transaction.setStatus(TransactionStatus.PROCESSING);
                    transactionRepository.save(transaction);

                    transactionService.executeTransaction(transaction);

                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transactionRepository.save(transaction);
                    log.info("Transaction {} processed successfully", transactionId);
                } catch (Exception e) {
                    log.error("Failed to process transaction {}: {}", transactionId, e.getMessage());
                    transaction.setStatus(TransactionStatus.FAILED);
                    transactionRepository.save(transaction);
                }
            }
        });
    }
}