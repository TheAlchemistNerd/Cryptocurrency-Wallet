package com.cryptowallet.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CryptoFacadeTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private KeyPairService keyPairService;

    @InjectMocks
    private CryptoFacade cryptoFacade;

    @Test
    void encryptData_shouldDelegateToEncryptionService() {
        // Arrange
        String data = "test-data";
        String encryptedData = "encrypted-data";
        when(encryptionService.encrypt(data)).thenReturn(encryptedData);

        // Act
        String result = cryptoFacade.encryptData(data);

        // Assert
        assertThat(result).isEqualTo(encryptedData);
        verify(encryptionService).encrypt(data);
        verifyNoMoreInteractions(encryptionService);
    }

    @Test
    void decryptData_shouldDelegateToEncryptionService() {
        // Arrange
        String encryptedData = "encrypted-data";
        String decryptedData = "decrypted-data";
        when(encryptionService.decrypt(encryptedData)).thenReturn(decryptedData);

        // Act
        String result = cryptoFacade.decryptData(encryptedData);

        // Assert
        assertThat(result).isEqualTo(decryptedData);
        verify(encryptionService).decrypt(encryptedData);
        verifyNoMoreInteractions(encryptionService);
    }

    @Test
    void signData_shouldDelegateToSignatureService() {
        // Arrange
        String data = "test-data";
        String keyIdentifier = "key-identifier";
        String signature = "signature";
        when(signatureService.sign(data, keyIdentifier)).thenReturn(signature);

        // Act
        String result = cryptoFacade.signData(data, keyIdentifier);

        // Assert
        assertThat(result).isEqualTo(signature);
        verify(signatureService).sign(data, keyIdentifier);
        verifyNoMoreInteractions(signatureService);
    }

    @Test
    void verifySignature_shouldDelegateToSignatureService() {
        // Arrange
        String data = "test-data";
        String signature = "signature";
        String publicKey = "public-key";
        when(signatureService.verifySignature(data, signature, publicKey)).thenReturn(true);

        // Act
        boolean result = cryptoFacade.verifySignature(data, signature, publicKey);

        // Assert
        assertThat(result).isTrue();
        verify(signatureService).verifySignature(data, signature, publicKey);
        verifyNoMoreInteractions(signatureService);
    }

    @Test
    void generateKeyPair_shouldDelegateToKeyPairService() {
        // Arrange
        EncodedKeyPair keyPair = new EncodedKeyPair("public", "private");
        when(keyPairService.generateKeyPair()).thenReturn(keyPair);

        // Act
        EncodedKeyPair result = cryptoFacade.generateKeyPair();

        // Assert
        assertThat(result).isEqualTo(keyPair);
        verify(keyPairService).generateKeyPair();
        verifyNoMoreInteractions(keyPairService);
    }
}
