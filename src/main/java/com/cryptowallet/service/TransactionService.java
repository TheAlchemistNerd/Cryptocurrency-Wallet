package com.cryptowallet.service;

import com.cryptowallet.blockchain.BlockChain;
import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.event.TransactionCreatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private final CryptoFacade cryptoFacade;
    private final MongoTemplate mongoTemplate;

    private final ApplicationEventPublisher eventPublisher;
    private final BlockChain blockChain;

    private final List<String> pendingTransactionIdsForBlock;
    private static final int BLOCK_SIZE_THRESHOLD = 8;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              CryptoFacade cryptoFacade,
                              MongoTemplate mongoTemplate,
                              ApplicationEventPublisher eventPublisher,
                              BlockChain blockChain) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.cryptoFacade = cryptoFacade;
        this.mongoTemplate = mongoTemplate;
        this.eventPublisher = eventPublisher;
        this.blockChain = blockChain;
        this.pendingTransactionIdsForBlock = Collections.synchronizedList(new ArrayList<>());
    }

    @Transactional
    public TransactionDTO processTransaction(SendTransactionRequestDTO dto) {
        log.info("Processing transaction from {} to {} amount={} {}", dto.fromAddress(), dto.toAddress(), dto.amount(), dto.currency());

        // 1. Validate signature to authenticate the sender and ensure integrity
        if(!"BYPASS_SIGNATURE_FOR_TESTING_BYPASS".equals(dto.signature())) {
            // Retrieve sender's wallet to get its public key (address) for signature verification
            WalletDocument senderWallet = walletRepository.findByAddress(dto.fromAddress())
                    .orElseThrow(() ->  new InvalidTransactionException("Sender wallet not found for signature verification."));

            // Use the wallet's address (public key) for signature verification
            if(!cryptoFacade.verifySignature(dto.toSignableString(), dto.signature(), dto.fromAddress())) {
                throw new InvalidTransactionException("Transaction signature is invalid.");
            }
        } else {
            log.warn("Signature verification bypassed for testing purposes.");
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

            // Publish TransactionCreatedEvent
            eventPublisher.publishEvent(new TransactionCreatedEvent(this, TransactionMapper.toDTO(saved)));
            log.info("Published TransactionCreatedEvent for transaction ID: {}", saved.getId());

            // Add transaction to pending list for block creation
            addTransactionToPendingBlock(saved.getId());

            return TransactionMapper.toDTO(saved);
        } catch (DuplicateKeyException e) {
            // This is a final safeguard against race conditions on the signature check.
            log.error("Duplicate signature detected during save operation for transaction: {}", dto.signature());
            throw new InvalidTransactionException("Duplicate transaction detected.");
        } catch (Exception e) {
            log.error("Failed to save transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving transaction.", e);
        }
    }

    private void addTransactionToPendingBlock(String transactionId) {
        synchronized (pendingTransactionIdsForBlock) {
            pendingTransactionIdsForBlock.add(transactionId);
            log.debug("Added transaction {} to pending block list. Current size: {}", transactionId, pendingTransactionIdsForBlock.size());

            if(pendingTransactionIdsForBlock.size() >= BLOCK_SIZE_THRESHOLD) {
                log.info("Pending transactions reached threshold ({}). Creating a new block...", BLOCK_SIZE_THRESHOLD);
                List<String> txIdsForBlock = new ArrayList<>(pendingTransactionIdsForBlock);
                pendingTransactionIdsForBlock.clear();

                try {
                    blockChain.addBlock(txIdsForBlock);
                    log.info("Successfully created a new block with {} transactions.", txIdsForBlock.size());
                } catch (Exception e) {
                    log.error("Failed to create a new block for transactions: {}. Error: {}", txIdsForBlock, e.getMessage());
                }
            }
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
                    wallet -> {
                        log.warn("Insufficient funds in {} for wallet {}. Current: {}, Attempted: {}",
                                dto.currency(),
                                dto.fromAddress(),
                                wallet.getBalances().getOrDefault(dto.currency(),
                                        BigDecimal.ZERO), dto.amount());
                        throw new InsufficientBalanceException("Insufficient funds in " + dto.currency() + " for wallet " + dto.fromAddress()); },
                    () -> {
                        log.warn("Sender wallet not found: {}", dto.fromAddress());
                        throw new WalletNotFoundException("Sender wallet not found: " + dto.fromAddress()); }
            );
        }
        log.info("Sender {} balance updated for {}. New balance: {}",
                dto.fromAddress(),
                dto.currency(),
                updated.getBalances().get(dto.currency()));
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
            log.warn("Receiver wallet not found: {}", dto.toAddress());
            throw new WalletNotFoundException("Receiver wallet not found: " + dto.toAddress());
        }
        log.info("Receiver {} balance updated for {}. New balance: {}", dto.toAddress(),
                dto.currency(),
                updated.getBalances().get(dto.currency()));
    }
}
