package com.cryptowallet.domain;

public class Wallet {
    private String walletId;
    private String publicKey;
    private String encryptedPrivateKey;
    private double balance;

    public Wallet(String walletId, String publicKey, String encryptedPrivateKey, double balance) {
        this.walletId = walletId;
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.balance = balance;
    }

    public String getWalletId() {
        return walletId;
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

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
