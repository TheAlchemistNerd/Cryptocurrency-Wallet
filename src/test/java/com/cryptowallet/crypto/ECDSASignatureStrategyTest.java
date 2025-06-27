package com.cryptowallet.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ECDSASignatureStrategyTest {
    private final ECDSASignatureStrategy strategy = new ECDSASignatureStrategy();

    @Test
    void testSignAndVerify() {
        String data = "Test transaction";
        EncodedKeyPair keyPair = strategy.generateKeyPair();
        String signature = strategy.sign(data, keyPair.getPrivate());
        assertTrue(strategy.verify(data, signature, keyPair.getPublic()));
    }

    @Test
    void testVerifyWithWrongSignature() {
        String data = "Test transaction";
        EncodedKeyPair keyPair = strategy.generateKeyPair();
        String signature = strategy.sign("Different data", keyPair.getPrivate());
        assertFalse(strategy.verify(data, signature, keyPair.getPublic()));
    }
}