package com.cryptowallet.domain;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletFactoryTest {

    private CryptoFacade cryptoFacade;
    private WalletFactory walletFactory;

    @BeforeEach
    void setup() {
        cryptoFacade = mock(CryptoFacade.class);
        walletFactory = new WalletFactory(cryptoFacade);
    }

    @Test
    void testCreateWallet() {
        String dummyPublicKey = "publicKeyBase64";
        String dummyEncryptedPrivateKey = "encryptedPrivateKeyBase64";

        EncodedKeyPair mockKeyPair = new EncodedKeyPair(dummyPublicKey, "privateKeyBase64");

        when(cryptoFacade.generateKeyPair()).thenReturn(mockKeyPair);
        when(cryptoFacade.encryptData("privateKeyBase64")).thenReturn(dummyEncryptedPrivateKey);

        Wallet wallet = walletFactory.createWallet("user123");

        assertNotNull(wallet);
        assertEquals(dummyPublicKey, wallet.getPublicKey());
        assertEquals(dummyEncryptedPrivateKey, wallet.getEncryptedPrivateKey());
        assertEquals(0.0, wallet.getBalance(), 0.001);

        verify(cryptoFacade).generateKeyPair();
        verify(cryptoFacade).encryptData("privateKeyBase64");
    }
}