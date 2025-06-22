package com.cryptowallet;

import com.cryptowallet.domain.Transaction;
import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void testTransactionBuilder() {
        Date now = new Date();
        Transaction txn = new Transaction.Builder()
            .transactionId("tx001")
            .senderAddress("addr1")
            .receiverAddress("addr2")
            .amount(123.45)
            .timestamp(now)
            .signature("sigXYZ")
            .build();

        assertEquals("tx001", txn.getTransactionId());
        assertEquals("addr1", txn.getSenderAddress());
        assertEquals("addr2", txn.getReceiverAddress());
        assertEquals(123.45, txn.getAmount());
        assertEquals(now, txn.getTimestamp());
        assertEquals("sigXYZ", txn.getSignature());
    }
}