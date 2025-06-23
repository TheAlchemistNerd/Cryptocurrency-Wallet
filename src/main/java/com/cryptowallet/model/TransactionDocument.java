package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("transactions")
public class TransactionDocument {
    @Id
    private String id;
    private String senderAddress;
    private String receiverAddress;
    private double amount;
    private Date timestamp;
    private String signature;

    public TransactionDocument() {}

    public TransactionDocument( String senderAddress, String receiverAddress, double amount, Date timestamp, String signature) {
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.amount = amount;
        this.timestamp = timestamp;
        this.signature = signature;
    }

    public String getId() {
        return id;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
