package com.cryptowallet.crypto;

public class EncryptionService {
    private final EncryptionStrategy strategy;

    public EncryptionService(EncryptionStrategy strategy) {
        this.strategy = strategy;
    }

    public String encrypt(String data) {
        return strategy.encrypt(data);
    }

    public String decrypt(String cipher) {
        return strategy.decrypt(cipher);
    }
}
