package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.exception.InsufficientBalanceException;
import com.cryptowallet.exception.InvalidTransactionException;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.TransactionRepository;
import com.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CryptoFacade cryptoFacade;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void processTransaction_shouldSucceed_whenRequestIsValid() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO(
                "senderAddress", "receiverAddress", new BigDecimal("100"), "validSignature", "BTC");

        WalletDocument senderWallet = new WalletDocument("user1", "senderAddress", "encKey1");
        senderWallet.setBalances(Map.of("BTC", new BigDecimal("200")));

        WalletDocument receiverWallet = new WalletDocument("user2", "receiverAddress", "encKey2");

        when(cryptoFacade.verifySignature(dto.toSignableString(), "validSignature", "senderAddress")).thenReturn(true);
        when(transactionRepository.existsBySignature("validSignature")).thenReturn(false);

        // --- FIX: The ambiguous call is resolved by using a slightly less specific `any()` for the options ---
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(WalletDocument.class)))
                .thenReturn(senderWallet)   // First call for sender
                .thenReturn(receiverWallet); // Second call for receiver

        when(transactionRepository.save(any(TransactionDocument.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        TransactionDTO result = transactionService.processTransaction(dto);

        // Assert
        assertThat(result.amount()).isEqualTo(new BigDecimal("100"));
        assertThat(result.fromAddress()).isEqualTo("senderAddress");
        assertThat(result.signature()).isEqualTo("validSignature");

        verify(cryptoFacade).verifySignature(anyString(), anyString(), anyString());
        verify(transactionRepository).existsBySignature(anyString());
        // Verify findAndModify was called exactly twice
        verify(mongoTemplate, times(2)).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(WalletDocument.class));
        verify(transactionRepository).save(any(TransactionDocument.class));
    }

    @Test
    void processTransaction_shouldThrowInvalidTransactionException_whenSignatureIsInvalid() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO(
                "senderAddress", "receiverAddress", new BigDecimal("100"), "invalidSignature", "BTC");

        when(cryptoFacade.verifySignature(anyString(), anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction signature is invalid.");

        verifyNoInteractions(mongoTemplate, transactionRepository);
    }

    @Test
    void processTransaction_shouldThrowInvalidTransactionException_whenSignatureIsReplayed() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO(
                "senderAddress", "receiverAddress", new BigDecimal("100"), "replayedSignature", "BTC");

        when(cryptoFacade.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);
        when(transactionRepository.existsBySignature("replayedSignature")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Duplicate transaction: this signature has already been processed.");

        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void processTransaction_shouldThrowInsufficientBalanceException_whenSenderBalanceIsTooLow() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO(
                "senderAddress", "receiverAddress", new BigDecimal("100"), "validSignature", "BTC");

        when(cryptoFacade.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);
        when(transactionRepository.existsBySignature(anyString())).thenReturn(false);

        // Simulate findAndModify failing due to insufficient balance (returns null)
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(WalletDocument.class)))
                .thenReturn(null);

        // Mock the repository to confirm the wallet exists, so the correct exception is thrown.
        when(walletRepository.findByAddress("senderAddress")).thenReturn(Optional.of(new WalletDocument()));

        // Act & Assert
        // --- FIX: Changed exception class to match the one in your service ---
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient funds in BTC for wallet senderAddress");
    }

    @Test
    void processTransaction_shouldThrowWalletNotFoundException_whenSenderWalletIsNotFound() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO(
                "senderAddress", "receiverAddress", new BigDecimal("100"), "validSignature", "BTC");

        when(cryptoFacade.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);
        when(transactionRepository.existsBySignature(anyString())).thenReturn(false);

        // --- FIX: The mock call is now unambiguous ---
        // Simulate findAndModify failing because wallet doesn't exist
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(WalletDocument.class))).thenReturn(null);
        when(walletRepository.findByAddress("senderAddress")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Sender wallet not found: senderAddress");
    }

    @Test
    void processTransaction_shouldThrowWalletNotFoundException_whenReceiverWalletIsNotFound() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO(
                "senderAddress", "receiverAddress", new BigDecimal("100"), "validSignature", "BTC");

        when(cryptoFacade.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);
        when(transactionRepository.existsBySignature(anyString())).thenReturn(false);

        // --- FIX: The mock call is now unambiguous ---
        // First call for sender succeeds, second for receiver fails
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(WalletDocument.class)))
                .thenReturn(new WalletDocument()) // Sender succeeds
                .thenReturn(null);              // Receiver fails

        // Act & Assert
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Receiver wallet not found: receiverAddress");
    }
}
