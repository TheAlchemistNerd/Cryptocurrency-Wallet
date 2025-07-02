package com.cryptowallet.service;

import com.cryptowallet.event.TransactionCreatedEvent;
import com.cryptowallet.event.UserRegisteredEvent;
import com.cryptowallet.event.WalletCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service responsible for auditing system events, specifically transaction creation.
 * Implements the Observer Pattern, listening for various application events.
 */

@Service
@Slf4j
public class AuditService {

    /**
     * Listens for UserRegisteredEvent and logs the details for auditing purposes.
     * @param event The UserRegisteredEvent containing details of the new user.
     */
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("AUDIT: User Registered Event Received - User ID: {}, Username: {}, Email: {}",
                event.getUserDTO().id(),
                event.getUserDTO().userName(),
                event.getUserDTO().email());
        // In a real system, this could trigger an external user onboarding workflow,
        // send a welcome email, or update an internal CRM.
    }

    /**
     * Listens for WalletCreatedEvent and logs the details for auditing purposes.
     * @param event The WalletCreatedEvent containing details of the new wallet.
     */
    @EventListener
    public void handleWalletCreatedEvent(WalletCreatedEvent event) {
        log.info("AUDIT: Wallet Created Event Received - Wallet ID: {}, User ID: {}, Address: {}, Initial Balances: {}",
                event.getWalletDTO().id(),
                event.getWalletDTO().userId(),
                event.getWalletDTO().address(),
                event.getWalletDTO().balances());
        // This could be used to trigger a notification to the user,
        // or to update a dashboard showing new wallet creations.
    }

    /**
     * Listens for TransactionCreatedEvent and logs the details for auditing purposes.
     * @param event The TransactionCreatedEvent containing details of the new transaction.
     */
    @EventListener
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.info("AUDIT: Transaction Created Event Received - Transaction ID: {}, From: {}, To: {}, Amount: {}, Currency: {}, Timestamp: {}",
                event.getTransactionDTO().id(),
                event.getTransactionDTO().fromAddress(),
                event.getTransactionDTO().toAddress(),
                event.getTransactionDTO().amount(),
                event.getTransactionDTO().currency(),
                event.getTransactionDTO().timestamp());
        // In a real application, this could write to a dedicated audit log,
        // send to a SIEM system, or trigger other external auditing processes.
    }
}
