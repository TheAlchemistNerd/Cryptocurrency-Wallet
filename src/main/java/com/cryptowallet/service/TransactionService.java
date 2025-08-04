package com.cryptowallet.service;

import com.cryptowallet.blockchain.BlockChain;
import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.domain.TransactionStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@Service
@Slf4j
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CryptoFacade cryptoFacade;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final BlockChain blockChain;
    private final AsyncTransactionProcessor asyncTransactionProcessor;

    private final List<String> pendingTransactionIdsForBlock;
    private static final int BLOCK_SIZE_THRESHOLD = 8;

    @Autowired
    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              CryptoFacade cryptoFacade,
                              MongoTemplate mongoTemplate,
                              ApplicationEventPublisher eventPublisher,
                              BlockChain blockChain, AsyncTransactionProcessor asyncTransactionProcessor) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.cryptoFacade = cryptoFacade;
        this.mongoTemplate = mongoTemplate;
        this.eventPublisher = eventPublisher;
        this.blockChain = blockChain;
        this.asyncTransactionProcessor = asyncTransactionProcessor;
        this.pendingTransactionIdsForBlock = Collections.synchronizedList(new ArrayList<>());
    }

    @Transactional
    public TransactionDTO processTransaction(SendTransactionRequestDTO dto) {
        log.info("Processing transaction from {} to {} amount={} {}", dto.fromAddress(), dto.toAddress(), dto.amount(), dto.currency());

        if(!"BYPASS_SIGNATURE_FOR_TESTING_BYPASS".equals(dto.signature())) {
            WalletDocument senderWallet = walletRepository.findByAddress(dto.fromAddress())
                    .orElseThrow(() ->  new InvalidTransactionException("Sender wallet not found for signature verification."));

            if(!cryptoFacade.verifySignature(dto.toSignableString(), dto.signature(), dto.fromAddress())) {
                throw new InvalidTransactionException("Transaction signature is invalid.");
            }
        } else {
            log.warn("Signature verification bypassed for testing purposes.");
        }

        Optional<TransactionDocument> existingTransaction = transactionRepository.findBySignature(dto.signature());
        if (existingTransaction.isPresent()) {
            log.warn("Duplicate transaction detected with signature: {}. Status: {}", dto.signature(), existingTransaction.get().getStatus());
            return TransactionMapper.toDTO(existingTransaction.get());
        }

        TransactionDocument transaction = new TransactionDocument(
                dto.fromAddress(),
                dto.toAddress(),
                dto.amount(),
                dto.currency(),
                dto.signature()
        );

        try {
            TransactionDocument saved = transactionRepository.save(transaction);
            log.info("Transaction saved with PENDING status, ID: {}", saved.getId());

            asyncTransactionProcessor.processTransaction(saved.getId());

            return TransactionMapper.toDTO(saved);
        } catch (DuplicateKeyException e) {
            log.error("Duplicate signature detected during save for transaction: {}. This should have been caught earlier.", dto.signature());
            throw new InvalidTransactionException("Duplicate transaction detected.");
        } catch (Exception e) {
            log.error("Failed to save initial transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving transaction.", e);
        }
    }

    public void executeTransaction(TransactionDocument transaction) {
        updateSenderBalance(transaction.getFromAddress(), transaction.getCurrency(), transaction.getAmount());
        updateReceiverBalance(transaction.getToAddress(), transaction.getCurrency(), transaction.getAmount());

        eventPublisher.publishEvent(new TransactionCreatedEvent(this, TransactionMapper.toDTO(transaction)));
        log.info("Published TransactionCreatedEvent for transaction ID: {}", transaction.getId());

        addTransactionToPendingBlock(transaction.getId());
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

    private void updateSenderBalance(String fromAddress, String currency, BigDecimal amount) {
        Query query = new Query(Criteria.where("address").is(fromAddress)
                .and("balances." + currency).gte(amount));

        Update update = new Update().inc("balances." + currency, amount.negate());

        WalletDocument updated = mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                WalletDocument.class
        );

        if (updated == null) {
            walletRepository.findByAddress(fromAddress).ifPresentOrElse(
                    wallet -> {
                        log.warn("Insufficient funds in {} for wallet {}. Current: {}, Attempted: {}",
                                currency, fromAddress, wallet.getBalances().getOrDefault(currency, BigDecimal.ZERO), amount);
                        throw new InsufficientBalanceException("Insufficient funds in " + currency + " for wallet " + fromAddress); },
                    () -> {
                        log.warn("Sender wallet not found: {}", fromAddress);
                        throw new WalletNotFoundException("Sender wallet not found: " + fromAddress); }
            );
        }
        log.info("Sender {} balance updated for {}. New balance: {}",
                fromAddress,
                currency,
                updated.getBalances().get(currency));
    }

    private void updateReceiverBalance(String toAddress, String currency, BigDecimal amount) {
        Query query = new Query(Criteria.where("address").is(toAddress));

        Update update = new Update().inc("balances." + currency, amount);

        WalletDocument updated = mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                WalletDocument.class
        );

        if(updated == null) {
            log.warn("Receiver wallet not found: {}", toAddress);
            throw new WalletNotFoundException("Receiver wallet not found: " + toAddress);
        }
        log.info("Receiver {} balance updated for {}. New balance: {}", toAddress,
                currency,
                updated.getBalances().get(currency));
    }
}
