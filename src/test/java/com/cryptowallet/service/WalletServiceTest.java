package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    private WalletRepository walletRepository;
    private CryptoFacade cryptoFacade;
    private WalletService walletService;

    @BeforeEach
    void setup() {
        walletRepository = mock(WalletRepository.class);
        cryptoFacade = mock(CryptoFacade.class);
        walletService = new WalletService(walletRepository, cryptoFacade);
    }

    @Test
    void shouldCreateWallet() {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO("user1");
        EncodedKeyPair keyPair = new EncodedKeyPair("pub", "priv");
        WalletDocument saved = new WalletDocument("user1", "pub", "encPriv", 0.0);
        saved.setId("w-001");

        when(cryptoFacade.generateKeyPair()).thenReturn(keyPair);
        when(cryptoFacade.encryptPrivateKey("priv")).thenReturn("encPriv");
        when(walletRepository.save(any())).thenReturn(saved);

        WalletDTO result = walletService.createWallet(dto);

        assertThat(result.walletId()).isEqualTo("w-001");
        assertThat(result.publicKey()).isEqualTo("pub");
        assertThat(result.userId()).isEqualTo("user1");
    }

    @Test
    void shouldReturnWalletById() {
        WalletDocument wallet = new WalletDocument("user1", "pub", "enc", 10.0);
        wallet.setId("wallet123");

        when(walletRepository.findById("wallet123")).thenReturn(Optional.of(wallet));

        WalletDTO dto = walletService.getWalletById("wallet123");

        assertThat(dto.walletId()).isEqualTo("wallet123");
    }

    @Test
    void shouldThrowIfWalletNotFound() {
        when(walletRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWalletById("bad-id"))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("Wallet is not found with ID");
    }
}
