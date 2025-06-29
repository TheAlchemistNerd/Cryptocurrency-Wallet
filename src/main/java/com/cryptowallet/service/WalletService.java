package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.mapper.WalletMapper;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final CryptoFacade cryptoFacade;


    public WalletService(WalletRepository walletRepository, CryptoFacade cryptoFacade) {
        this.walletRepository = walletRepository;
        this.cryptoFacade = cryptoFacade;
    }

    public WalletDTO createWallet(CreateWalletRequestDTO dto) {
        log.info("Generating key pair for userId={}", dto.userId());
        EncodedKeyPair keyPair = cryptoFacade.generateKeyPair();
        String encryptedPrivateKey = cryptoFacade.encryptPrivateKey(keyPair.getPrivate());

        WalletDocument wallet = WalletMapper.fromCreateDto(
                dto,
                keyPair.getPublic(),
                encryptedPrivateKey
        );

        WalletDocument saved = walletRepository.save(wallet);

        log.info("Wallet created with ID={} and address={} for userId={}", saved.getId(), saved.getAddress(), saved.getUserId());

        return WalletMapper.toDTO(saved);
    }

    public WalletDTO getWalletById(String walletId) {
        WalletDocument wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet is not found with ID: " + walletId));
        return WalletMapper.toDTO(wallet);
    }
}
