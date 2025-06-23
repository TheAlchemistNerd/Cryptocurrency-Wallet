package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wallets")
public class WalletDocument {
    @Id
    private String id;
    private String userId;
    private String publicKey;
    private String encryptedPrivateKey;
    private double balance;

    public WalletDocument() {}

    public WalletDocument(String userId, String publicKey, String encryptedPrivateKey, double balance) {
        this.userId = userId;
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public double getBalance() {
        return balance;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
