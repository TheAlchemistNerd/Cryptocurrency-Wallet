package com.cryptowallet.event;

import com.cryptowallet.dto.WalletDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new wallet is successfully created.
 * Contains the WalletDTO of the newly created wallet
 */
public class WalletCreatedEvent extends ApplicationEvent {

    private final WalletDTO walletDTO;

    /**
     * Create a new WalletCreatedEvent
     * @param source The object on which the event initially occurred (e.g WalletService)
     * @param walletDTO The Data Transfer object for the newly created wallet.
     */

    public WalletCreatedEvent(Object source, WalletDTO walletDTO) {
        super(source);
        this.walletDTO = walletDTO;
    }

    /**
     * Returns the details of the created wallet.
     * @return The WalletDTO associated with this event
     */
    public WalletDTO getWalletDTO() {
        return walletDTO;
    }
}
