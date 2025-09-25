package com.cryptowallet.crypto;

/**
 * A dedicated service for cryptographic key pair generation.
 * It uses the KeyPairGenerator strategy to perform the generation.
 */
public class KeyPairService {
    private final KeyPairGenerator keyPairGenerator;

    public KeyPairService(KeyPairGenerator keyPairGenerator) {
        this.keyPairGenerator = keyPairGenerator;
    }

    public EncodedKeyPair generateKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }
}