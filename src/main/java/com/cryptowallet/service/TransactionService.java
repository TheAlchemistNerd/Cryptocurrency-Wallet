package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.exception.InsufficientBalanceException;
import com.cryptowallet.exception.InvalidTransactionException;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.mapper.TransactionMapper;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.TransactionRepository;
import com.cryptowallet.repository.WalletRepository;
import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private final CryptoFacade cryptoFacade;
    private final MongoTemplate mongoTemplate;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              CryptoFacade cryptoFacade,
                              MongoTemplate mongoTemplate) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.cryptoFacade = cryptoFacade;
        this.mongoTemplate = mongoTemplate;
    }

    public TransactionDTO processTransaction(SendTransactionRequestDTO dto) {
        log.info("Processing transaction from {} to {} amount={} {}", dto.fromAddress(), dto.toAddress(), dto.amount(), dto.currency());

        // 1. Validate signature to authenticate the sender and ensure integrity
        if(!cryptoFacade.verifySignature(dto.toSignableString(), dto.signature(), dto.fromAddress())) {
            throw new InvalidTransactionException("Transaction signature is invalid.");
        }

        // 2. Prevent replay attacks by checking if the signature has been used before.
        // This is more robust than the old idempotency key.
        if(transactionRepository.existsBySignature(dto.signature())) {
            throw new InvalidTransactionException("Duplicate transaction: this signature has already been processed.");
        }

        // 3. Atomically update balances
        updateSenderBalance(dto);
        updateReceiverBalance(dto);

        // 4. Record the transaction
        TransactionDocument transaction = new TransactionDocument(
                dto.fromAddress(),
                dto.toAddress(),
                dto.amount(),
                dto.currency(),
                dto.signature()
        );

        try {
            TransactionDocument saved = transactionRepository.save(transaction);
            log.info("Transaction completed and saved with ID: {}", saved.getId());
            return TransactionMapper.toDTO(saved);
        } catch (DuplicateKeyException e) {
            // This is a final safeguard against race conditions on the signature check.
            log.error("Duplicate signature detected during save operation for transaction: {}", dto.signature());
            throw new InvalidTransactionException("Duplicate transaction detected.");
        }
    }

    private void updateSenderBalance(SendTransactionRequestDTO dto) {
        // Query to find the sender's wallet and ensure it has enough funds in the specified currency.
        Query query = new Query(Criteria.where("address").is(dto.fromAddress())
                .and("balances." + dto.currency()).gte(dto.amount()));

        // Atomically decrement the balance for the specified currency.
        Update update = new Update().inc("balances." + dto.currency(), dto.amount().negate());

        WalletDocument updated = mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                WalletDocument.class
        );

        if (updated == null) {
            // If it fails, check if the wallet exists at all to give a better error message.
            walletRepository.findByAddress(dto.fromAddress()).ifPresentOrElse(
                    wallet -> { throw new InsufficientBalanceException("Insufficient funds in " + dto.currency() + " for wallet " + dto.fromAddress()); },
                    () -> { throw new WalletNotFoundException("Sender wallet not found: " + dto.fromAddress()); }
            );
        }
    }

    private void updateReceiverBalance(SendTransactionRequestDTO dto) {
        Query query = new Query(Criteria.where("address").is(dto.toAddress()));

        // Atomically increment the balance for the specified currency.
        // The 'upsert' option is not used here; receiver wallets must exist.
        Update update = new Update().inc("balances." + dto.currency(), dto.amount());

        WalletDocument updated = mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                WalletDocument.class
        );

        if(updated == null) {
            throw new WalletNotFoundException("Receiver wallet not found: " + dto.toAddress());
        }
    }
}
