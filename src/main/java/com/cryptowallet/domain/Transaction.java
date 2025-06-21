package com.cryptowallet.domain;

import java.util.Date;

public class Transaction {
    private final String transactionId;
    private final String senderAddress;
    private final String receiverAddress;
    private final double amount;
    private final Date timestamp;
    private final String signature;

    private Transaction (Builder builder) {
        this.transactionId = builder.transactionId;
        this.senderAddress = builder.senderAddress;
        this.receiverAddress =builder.receiverAddress;
        this.amount = builder.amount;
        this.timestamp = builder.timestamp;
        this.signature = builder.signature;

    }

    public static class Builder {
        private String transactionId;
        private String senderAddress;
        private String receiverAddress;
        private double amount;
        private Date timestamp;
        private String signature;

        public Builder transactionId(String id) {
            this.transactionId = id; return this;
        }

        public Builder senderAddress(String sender) {
            this.senderAddress = sender; return this;
        }

        public Builder receiverAddress(String receiver) {
            this.receiverAddress = receiver; return this;
        }

        public Builder amount(double amt) {
            this.amount = amt; return this;
        }

        public Builder timestamp(Date time) {
            this.timestamp = time; return this;
        }

        public Builder signature(String sign) {
            this.signature = sign; return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }

    public String getTransactionId() {
        return transactionId;
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
}
