package com.cryptowallet.service;

import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.mapper.TransactionMapper;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.TransactionRepository;
import com.cryptowallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MongoTemplate mongoTemplate;

    public TransactionService(WalletRepository walletRepository, TransactionRepository transactionRepository, MongoTemplate mongoTemplate) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public TransactionDTO processTransaction(SendTransactionRequestDTO dto) {
        log.info("Initiating transaction from {} to {} amount={}", dto.senderWalletId(), dto.receiverAddress(), dto.amount());

        // Check for idempotent request
        Optional<TransactionDocument> existing = transactionRepository.findBySenderAddress(dto.senderWalletId())
                .stream()
                .filter(tx -> tx.getSignature().equals(dto.generateIdempotencyKey()))
                .findFirst();

        if(existing.isPresent()) {
            log.warn("Duplicate transaction detected: {}", dto.generateIdempotencyKey());
            return TransactionMapper.toDTO(existing.get());
        }

        // Atomic balance decrement on sender
        /*
        Query senderQuery = new Query(Criteria.where("_id").is(dto.senderWalletId()).and("balance").gte(dto.amount()));
        Update senderUpdate = new Update().inc("balance", -dto.amount());
        WalletDocument updatedSender = mongoTemplate.findAndModify(
                senderQuery,
                senderUpdate,
                FindAndModifyOptions.options().returnNew(true),
                WalletDocument.class
        );

        if(updatedSender == null) {
            log.error("Sender not found or insufficient balance");
            throw new WalletNotFoundException("Sender not found or insufficient balance");
        }
         */

        WalletDocument updatedSender = updateWalletBalance(
                "_id", dto.senderWalletId(), -dto.amount(),
                "Sender not found or insufficient balance"
        );


        // Atomic balance increment on receiver
        /*
        Query receiverQuery = new Query(Criteria.where("publicKey").is(dto.receiverAddress()));
        Update receiverUpdate = new Update().inc("balance", dto.amount());
        WalletDocument updatedReceiver = mongoTemplate.findAndModify(
                receiverQuery,
                receiverUpdate,
                FindAndModifyOptions.options().returnNew(true),
                WalletDocument.class
        );

        if (updatedReceiver == null) {
            log.error("Receiver wallet not found for address: {}", dto.receiverAddress());
            throw new WalletNotFoundException("Receiver not found");
        }
         */

        WalletDocument updatedReceiver = updateWalletBalance(
                "publicKey", dto.receiverAddress(), dto.amount(),
                "Receiver not found"
        );



        TransactionDocument transaction = new TransactionDocument(
            updatedSender.getPublicKey(),
            updatedReceiver.getPublicKey(),
            dto.amount(),
            new Date(),
            dto.generateIdempotencyKey()
        );

        TransactionDocument saved = transactionRepository.save(transaction);
        log.info("Transaction completed and saved: {}", saved.getId());

        return TransactionMapper.toDTO(saved);
    }

    private WalletDocument updateWalletBalance(String field, Object value, double delta, String notFoundMessage) {
        Query query = new Query(Criteria.where(field).is(value));
        if (delta < 0) {
            query.addCriteria(Criteria.where("balance").gte(Math.abs(delta))); // ensure sufficient balance
        }

        Update update = new Update().inc("balance", delta);

        WalletDocument updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                WalletDocument.class
        );

        if (updated == null) {
            log.error(notFoundMessage);
            throw new WalletNotFoundException(notFoundMessage);
        }

        return updated;
    }
}
