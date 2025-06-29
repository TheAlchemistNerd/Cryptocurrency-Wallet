package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private CryptoFacade cryptoFacade;
    @InjectMocks
    private WalletService walletService;

    @Test
    void shouldCreateWallet() {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO("user-id-1");
        EncodedKeyPair keyPair = new EncodedKeyPair("public_key", "private_key");
        WalletDocument savedDoc = new WalletDocument("user-id-1", "public_key", "encrypted_private_key");
        savedDoc.setId("wallet-id-1");

        when(cryptoFacade.generateKeyPair()).thenReturn(keyPair);
        when(cryptoFacade.encryptPrivateKey("private_key")).thenReturn("encrypted_private_key");
        when(walletRepository.save(any(WalletDocument.class))).thenReturn(savedDoc);

        WalletDTO result = walletService.createWallet(dto);

        assertThat(result.id()).isEqualTo("wallet-id-1");
        assertThat(result.address()).isEqualTo("public_key");
        assertThat(result.userId()).isEqualTo("user-id-1");
        assertThat(result.balances()).containsKey("USD");
    }

    @Test
    void shouldThrowIfWalletNotFound() {
        when(walletRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWalletById("bad-id"))
                .isInstanceOf(WalletNotFoundException.class);
    }
}