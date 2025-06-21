package com.cryptowallet.crypto;

public interface EncryptionStrategy {
    String encrypt(String data);
    String decrypt(String cipherText);
}
