package com.cryptowallet.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ECDSASignatureStrategyTest {

    @Mock
    private PrivateKeyResolver privateKeyResolver;

    @InjectMocks
    private ECDSASignatureStrategy ecdsaSignatureStrategy;

    private EncodedKeyPair keyPair;

    @BeforeAll
    static void beforeAll() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeEach
    void setUp() {
        // Generate a real key pair for testing
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        keyPair = keyPairGenerator.generateKeyPair();
    }

    @Test
    void sign_shouldReturnValidSignature() {
        // Arrange
        String data = "test-data";
        String keyIdentifier = "key-identifier";
        when(privateKeyResolver.findDecryptedPrivateKeyByIdentifier(keyIdentifier)).thenReturn(keyPair.getPrivate());

        // Act
        String signature = ecdsaSignatureStrategy.sign(data, keyIdentifier);

        // Assert
        assertThat(signature).isNotNull();
        boolean isValid = ecdsaSignatureStrategy.verify(data, signature, keyPair.getPublic());
        assertThat(isValid).isTrue();
    }

    @Test
    void verify_shouldReturnTrue_whenSignatureIsValid() {
        // Arrange
        String data = "test-data";
        String keyIdentifier = "key-identifier";
        when(privateKeyResolver.findDecryptedPrivateKeyByIdentifier(keyIdentifier)).thenReturn(keyPair.getPrivate());
        String signature = ecdsaSignatureStrategy.sign(data, keyIdentifier);

        // Act
        boolean result = ecdsaSignatureStrategy.verify(data, signature, keyPair.getPublic());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void verify_shouldReturnFalse_whenSignatureIsInvalid() {
        // Arrange
        String data = "test-data";
        String signature = "invalid-signature";

        // Act
        boolean result = ecdsaSignatureStrategy.verify(data, signature, keyPair.getPublic());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_shouldReturnFalse_whenDataIsTampered() {
        // Arrange
        String data = "test-data";
        String tamperedData = "tampered-data";
        String keyIdentifier = "key-identifier";
        when(privateKeyResolver.findDecryptedPrivateKeyByIdentifier(keyIdentifier)).thenReturn(keyPair.getPrivate());
        String signature = ecdsaSignatureStrategy.sign(data, keyIdentifier);

        // Act
        boolean result = ecdsaSignatureStrategy.verify(tamperedData, signature, keyPair.getPublic());

        // Assert
        assertThat(result).isFalse();
    }
}