package com.cryptowallet.crypto;

public class SignatureService {
    private final SignatureStrategy strategy;

    public SignatureService(SignatureStrategy strategy) {
        this.strategy = strategy;
    }

    public String sign(String data, String keyAddressIdentifier) {
        return strategy.sign(data, keyAddressIdentifier);
    }

    public boolean verifySignature(String data, String signature, String publicKey) {
        return strategy.verify(data, signature, publicKey);
    }
}