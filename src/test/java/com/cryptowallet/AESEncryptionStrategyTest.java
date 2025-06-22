package com.cryptowallet;

import com.cryptowallet.crypto.AESEncryptionStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptionStrategyTest {
    private final String key = "12345678901234567890123456789012"; // 32 bytes
    private final AESEncryptionStrategy aes = new AESEncryptionStrategy(key);

    @Test
    void testEncryptDecrypt() {
        String data = "Sensitive data";
        String encrypted = aes.encrypt(data);
        String decrypted = aes.decrypt(encrypted);
        assertEquals(data, decrypted);
    }

    @Test
    void testEmptyString() {
        String encrypted = aes.encrypt("");
        String decrypted = aes.decrypt(encrypted);
        assertEquals("", decrypted);
    }

    @Test
    void testWrongKeyLength() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AESEncryptionStrategy("short-key");
        });
        assertTrue(exception.getMessage().contains("AES key must be 32 bytes"));
    }
}