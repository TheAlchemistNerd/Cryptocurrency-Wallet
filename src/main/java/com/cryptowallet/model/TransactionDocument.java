package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("transactions")
public class TransactionDocument {
    @Id
    private String id;

    @Indexed
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private String currency;
    private Instant timestamp;
    @Indexed(unique = true) // The signature should be unique to prevent replay attacks
    private String signature;

    public TransactionDocument() {}

    public TransactionDocument( String fromAddress, String toAddress, BigDecimal amount, String currency, String signature) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.currency = currency;
        this.signature = signature;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
