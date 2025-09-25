package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.model.SecretStorageDocument;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.SecretStorageRepository;
import com.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private SecretStorageRepository secretStorageRepository;

    @Mock
    private CryptoFacade cryptoFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletService walletService;

    @Test
    void createWallet_shouldGenerateKeysAndStoreSecretSeparately() {
        // Arrange
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO("user-id-1");
        EncodedKeyPair keyPair = new EncodedKeyPair("public_key", "private_key");
        String encryptedPrivateKey = "encrypted_private_key";

        when(cryptoFacade.generateKeyPair()).thenReturn(keyPair);
        when(cryptoFacade.encryptData("private_key")).thenReturn(encryptedPrivateKey);

        // When walletRepository.save is called, return the same document but with an ID set
        when(walletRepository.save(any(WalletDocument.class))).thenAnswer(invocation -> {
            WalletDocument doc = invocation.getArgument(0);
            doc.setId("wallet-id-1");
            return doc;
        });

        // Act
        WalletDTO result = walletService.createWallet(dto);

        // Assert
        assertThat(result.id()).isEqualTo("wallet-id-1");
        assertThat(result.address()).isEqualTo("public_key");
        assertThat(result.userId()).isEqualTo("user-id-1");

        // Verify that the wallet document was saved without the private key
        ArgumentCaptor<WalletDocument> walletCaptor = ArgumentCaptor.forClass(WalletDocument.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertThat(walletCaptor.getValue().getAddress()).isEqualTo("public_key");

        // Verify that the secret storage document was saved with the encrypted private key
        ArgumentCaptor<SecretStorageDocument> secretCaptor = ArgumentCaptor.forClass(SecretStorageDocument.class);
        verify(secretStorageRepository).save(secretCaptor.capture());
        assertThat(secretCaptor.getValue().getId()).isEqualTo("public_key");
        assertThat(secretCaptor.getValue().getEncryptedPrivateKey()).isEqualTo(encryptedPrivateKey);

        // Verify facade calls
        verify(cryptoFacade).generateKeyPair();
        verify(cryptoFacade).encryptData("private_key");
    }

    @Test
    void getWalletById_shouldReturnWallet_whenFound() {
        // Arrange
        WalletDocument walletDoc = new WalletDocument("user-id-1", "public_key");
        walletDoc.setId("wallet-id-1");
        when(walletRepository.findById("wallet-id-1")).thenReturn(Optional.of(walletDoc));

        // Act
        WalletDTO result = walletService.getWalletById("wallet-id-1");

        // Assert
        assertThat(result.id()).isEqualTo("wallet-id-1");
        assertThat(result.address()).isEqualTo("public_key");
    }

    @Test
    void getWalletById_shouldThrowWalletNotFoundException_whenNotFound() {
        // Arrange
        when(walletRepository.findById("bad-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> walletService.getWalletById("bad-id"))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Wallet is not found with ID: bad-id");
    }
}
