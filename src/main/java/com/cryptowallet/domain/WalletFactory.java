package com.cryptowallet.domain;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;

import java.util.UUID;

public class WalletFactory {

    private final CryptoFacade cryptoFacade;

    public WalletFactory(CryptoFacade cryptoFacade) {
        this.cryptoFacade = cryptoFacade;
    }

    public Wallet createWallet(String userId) {
        EncodedKeyPair keyPair = cryptoFacade.generateKeyPair();
        String publicKey = keyPair.getPublic();
        String encryptedPrivateKey = cryptoFacade.encryptData(keyPair.getPrivate());
        String walletId = UUID.randomUUID().toString();

        return new Wallet(walletId, publicKey, encryptedPrivateKey, 0.0);
    }
}
