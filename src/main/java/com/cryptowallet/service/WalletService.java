package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final CryptoFacade cryptoFacade;


    public WalletService(WalletRepository walletRepository, CryptoFacade cryptoFacade) {
        this.walletRepository = walletRepository;
        this.cryptoFacade = cryptoFacade;
    }

    public WalletDTO createWallet(CreateWalletRequestDTO dto) {
        var keyPair = cryptoFacade.generateKeyPair();
        var encryptedPrivateKey = cryptoFacade.encryptPrivateKey(keyPair.getPrivate());

        WalletDocument wallet = new WalletDocument(
                dto.userId(),
                keyPair.getPublic(),
                encryptedPrivateKey,
                0.0
        );

        WalletDocument saved = walletRepository.save(wallet);

        return new WalletDTO(
                saved.getId(),
                saved.getUserId(),
                saved.getPublicKey(),
                saved.getBalance()
        );
    }
}
