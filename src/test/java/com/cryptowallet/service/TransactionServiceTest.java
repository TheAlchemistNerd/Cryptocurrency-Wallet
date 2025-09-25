package com.cryptowallet.service;

import com.cryptowallet.blockchain.BlockChain;
import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.exception.InvalidTransactionException;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.repository.TransactionRepository;
import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CryptoFacade cryptoFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AsyncTransactionProcessor asyncTransactionProcessor;

    @Mock
    private BlockChain blockChain;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void processTransaction_withClientSignature_shouldSucceed() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO("from", "to", new BigDecimal("10"), "sig", "BTC");
        when(cryptoFacade.verifySignature(dto.toSignableString(), "sig", "from")).thenReturn(true);
        when(transactionRepository.findBySignature("sig")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionDocument.class))).thenAnswer(i -> {
            TransactionDocument tx = i.getArgument(0);
            tx.setId("tx-id");
            return tx;
        });

        // Act
        TransactionDTO result = transactionService.processTransaction(dto);

        // Assert
        assertThat(result.signature()).isEqualTo("sig");
        verify(cryptoFacade).verifySignature(dto.toSignableString(), "sig", "from");
        verify(transactionRepository).save(any(TransactionDocument.class));
        verify(asyncTransactionProcessor).processTransaction("tx-id");
    }

    @Test
    void processTransaction_withoutClientSignature_shouldGenerateSignatureAndSucceed() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO("from", "to", new BigDecimal("10"), null, "BTC");
        when(cryptoFacade.signData(dto.toSignableString(), "from")).thenReturn("server-sig");
        when(transactionRepository.findBySignature("server-sig")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionDocument.class))).thenAnswer(i -> {
            TransactionDocument tx = i.getArgument(0);
            tx.setId("tx-id");
            return tx;
        });

        // Act
        TransactionDTO result = transactionService.processTransaction(dto);

        // Assert
        assertThat(result.signature()).isEqualTo("server-sig");
        verify(cryptoFacade).signData(dto.toSignableString(), "from");
        verify(transactionRepository).save(any(TransactionDocument.class));
        verify(asyncTransactionProcessor).processTransaction("tx-id");
    }

    @Test
    void processTransaction_shouldThrow_whenClientSignatureIsInvalid() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO("from", "to", new BigDecimal("10"), "invalid-sig", "BTC");
        when(cryptoFacade.verifySignature(dto.toSignableString(), "invalid-sig", "from")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction signature is invalid.");
        verifyNoInteractions(transactionRepository, asyncTransactionProcessor);
    }

    @Test
    void processTransaction_shouldReturnExistingTransaction_whenSignatureIsReplayed() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO("from", "to", new BigDecimal("10"), "replay-sig", "BTC");
        TransactionDocument existingTx = new TransactionDocument("from", "to", new BigDecimal("10"), "BTC", "replay-sig");
        when(cryptoFacade.verifySignature(dto.toSignableString(), "replay-sig", "from")).thenReturn(true);
        when(transactionRepository.findBySignature("replay-sig")).thenReturn(Optional.of(existingTx));

        // Act
        TransactionDTO result = transactionService.processTransaction(dto);

        // Assert
        assertThat(result.signature()).isEqualTo("replay-sig");
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(asyncTransactionProcessor);
    }

    @Test
    void processTransaction_shouldThrow_whenDuplicateKeyOnSave() {
        // Arrange
        SendTransactionRequestDTO dto = new SendTransactionRequestDTO("from", "to", new BigDecimal("10"), "sig", "BTC");
        when(cryptoFacade.verifySignature(dto.toSignableString(), "sig", "from")).thenReturn(true);
        when(transactionRepository.findBySignature("sig")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionDocument.class))).thenThrow(new DuplicateKeyException("Duplicate key"));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.processTransaction(dto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Duplicate transaction detected.");
    }
}