package com.cryptowallet.crypto;

public class SignatureService {
    private final SignatureStrategy strategy;

    public SignatureService(SignatureStrategy strategy) {
        this.strategy = strategy;
    }


    public EncodedKeyPair generateKeyPair() {
        return strategy.generateKeyPair();
    }

    public String sign(String data, String privateKey) {
        return strategy.sign(data, privateKey);
    }

    public boolean verifySignature(String data, String signature, String publicKey) {
        return strategy.verify(data, signature, publicKey);
    }
}
