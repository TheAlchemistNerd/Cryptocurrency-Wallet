package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Document(collection = "wallets")
public class WalletDocument {
    @Id
    private String id;
    private String userId;

    @Indexed(unique = true)
    private String address; // public key serves as the address
    private String encryptedPrivateKey;
    // Stores balances for different currencies, e.g., {"BTC": 1.5, "ETH": 10.2}
    private Map<String, BigDecimal> balances = new ConcurrentHashMap<>();

    public WalletDocument() {}

    public WalletDocument(String userId, String address, String encryptedPrivateKey) {
        this.userId = userId;
        this.address = address;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.balances.put("USD", new BigDecimal("1000.00")); // Starting "fiat" for new wallets
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getAddress() {
        return address;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public void setBalances(Map<String, BigDecimal> balances) {
        this.balances = balances;
    }
}
