package com.cryptowallet.crypto;

public interface SignatureStrategy {
    EncodedKeyPair generateKeyPair();
    String sign(String data, String base64PrivateKey);
    boolean verify(String data, String signature, String base64PublicKey);

}
