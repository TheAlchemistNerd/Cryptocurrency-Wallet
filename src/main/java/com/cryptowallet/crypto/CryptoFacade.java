package com.cryptowallet.crypto;

public class CryptoFacade {
    private final EncryptionService encryptionService;
    private final SignatureService signatureService;

    public CryptoFacade(EncryptionService encryptionService, SignatureService signatureService) {
        this.encryptionService = encryptionService;
        this.signatureService = signatureService;
    }

    public String encryptPrivateKey(String data) {
        return encryptionService.encrypt(data);
    }

    public String decryptPrivateKey(String cipherText) {
        return encryptionService.decrypt(cipherText);
    }

    public EncodedKeyPair generateKeyPair() {
        return signatureService.generateKeyPair();
    }

    public String signData(String data, String privateKey) {
        return signatureService.sign(data, privateKey);
    }

    public boolean verifySignature(String data, String signature, String publicKey) {
        return signatureService.verifySignature(data, signature, publicKey);
    }
}
