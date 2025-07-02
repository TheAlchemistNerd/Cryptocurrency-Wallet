package com.cryptowallet.event;

import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.service.TransactionService;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new transaction is successfully created & saved.
 * This is a POJO containing details about the transaction.
 */
public class TransactionCreatedEvent extends ApplicationEvent {
    private final TransactionDTO transactionDTO;
    /**
     * Create a new TransactionEvent.
     * @param source The transaction on which the transaction event initially occured .
     * @param transactionDTO The Data transfer Object for the new created transaction.
     */
    public TransactionCreatedEvent(Object source, TransactionDTO transactionDTO) {
        super(source);
        this.transactionDTO = transactionDTO;
    }

    /**
     * Returns the details of the created transaction.
     * @return The transactionDTO associated with this event.
     */
    public TransactionDTO getTransactionDTO() {
        return transactionDTO;
    }
}
