package com.cryptowallet;

import com.cryptowallet.crypto.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoFacadeTest {

    private final AESEncryptionStrategy aes = new AESEncryptionStrategy("12345678901234567890123456789012");
    private final ECDSASignatureStrategy ecdsa = new ECDSASignatureStrategy();

    private final CryptoFacade facade = new CryptoFacade(new EncryptionService(aes), new SignatureService(ecdsa));

    @Test
    void testKeyEncryptionCycle() {
        EncodedKeyPair pair = facade.generateKeyPair();
        String encrypted = facade.encryptPrivateKey(pair.getPrivate());
        String decrypted = facade.decryptPrivateKey(encrypted);
        assertEquals(pair.getPrivate(), decrypted);
    }

    @Test
    void testSignatureCycle() {
        String data = "Sample Data";
        EncodedKeyPair pair = facade.generateKeyPair();
        String signature = facade.signData(data, pair.getPrivate());
        assertTrue(facade.verifySignature(data, signature, pair.getPublic()));
    }

    @Test
    void testSignatureFailsForModifiedData() {
        String data = "Sample Data";
        EncodedKeyPair pair = facade.generateKeyPair();
        String signature = facade.signData(data, pair.getPrivate());
        assertFalse(facade.verifySignature("Tampered", signature, pair.getPublic()));
    }
}