package com.cryptowallet.crypto;

public class CryptoFacade {
    private final EncryptionService encryptionService;
    private final SignatureService signatureService;
    private final KeyPairService keyPairService;

    public CryptoFacade(EncryptionService encryptionService, SignatureService signatureService, KeyPairService keyPairService) {
        this.encryptionService = encryptionService;
        this.signatureService = signatureService;
        this.keyPairService = keyPairService;
    }

    public String encryptData(String data) {
        return encryptionService.encrypt(data);
    }

    public String decryptData(String cipherText) {
        return encryptionService.decrypt(cipherText);
    }

    public String signData(String data, String keyAddressIdentifier) {
        return signatureService.sign(data, keyAddressIdentifier);
    }

    public boolean verifySignature(String data, String signature, String publicKey) {
        return signatureService.verifySignature(data, signature, publicKey);
    }

    public EncodedKeyPair generateKeyPair() {
        return keyPairService.generateKeyPair();
    }
}