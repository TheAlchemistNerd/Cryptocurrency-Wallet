package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A dedicated document for storing sensitive key material, segregated from other data.
 * The ID is the wallet address (public key) to which the private key belongs.
 */
@Document(collection = "secrets")
public class SecretStorageDocument {

    @Id
    private String id; // Wallet Address

    private String encryptedPrivateKey;

    public SecretStorageDocument() {}

    public SecretStorageDocument(String id, String encryptedPrivateKey) {
        this.id = id;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
}
