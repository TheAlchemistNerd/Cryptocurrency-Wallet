package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.model.SecretStorageDocument;
import com.cryptowallet.repository.SecretStorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletPrivateKeyResolverTest {

    @Mock
    private SecretStorageRepository secretStorageRepository;

    @Mock
    private CryptoFacade cryptoFacade;

    @InjectMocks
    private WalletPrivateKeyResolver walletPrivateKeyResolver;

    @Test
    void findDecryptedPrivateKeyByIdentifier_shouldReturnDecryptedKey_whenFound() {
        // Arrange
        String keyIdentifier = "key-identifier";
        String encryptedKey = "encrypted-key";
        String decryptedKey = "decrypted-key";
        SecretStorageDocument secret = new SecretStorageDocument(keyIdentifier, encryptedKey);

        when(secretStorageRepository.findById(keyIdentifier)).thenReturn(Optional.of(secret));
        when(cryptoFacade.decryptData(encryptedKey)).thenReturn(decryptedKey);

        // Act
        String result = walletPrivateKeyResolver.findDecryptedPrivateKeyByIdentifier(keyIdentifier);

        // Assert
        assertThat(result).isEqualTo(decryptedKey);
    }

    @Test
    void findDecryptedPrivateKeyByIdentifier_shouldThrowWalletNotFoundException_whenNotFound() {
        // Arrange
        String keyIdentifier = "key-identifier";
        when(secretStorageRepository.findById(keyIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> walletPrivateKeyResolver.findDecryptedPrivateKeyByIdentifier(keyIdentifier))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("No private key found for identifier: " + keyIdentifier);
    }
}
