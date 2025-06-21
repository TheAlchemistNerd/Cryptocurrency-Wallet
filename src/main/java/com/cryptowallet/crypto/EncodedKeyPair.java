package com.cryptowallet.crypto;
/**
 * A simple DTO that holds Base64-encoded versions
 * of the public and private keys.
 */
public class EncodedKeyPair {
    private final String publicKey;
    private final String privateKey;

    public EncodedKeyPair(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPublic() { return publicKey; }
    public String getPrivate() { return privateKey; }
}

