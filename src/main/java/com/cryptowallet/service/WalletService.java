package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.EncodedKeyPair;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.mapper.WalletMapper;
import com.cryptowallet.model.SecretStorageDocument;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.SecretStorageRepository;
import com.cryptowallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final SecretStorageRepository secretStorageRepository;
    private final CryptoFacade cryptoFacade; // Reverted back to using the facade
    private final ApplicationEventPublisher eventPublisher;


    public WalletService(WalletRepository walletRepository, SecretStorageRepository secretStorageRepository, CryptoFacade cryptoFacade, ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.secretStorageRepository = secretStorageRepository;
        this.cryptoFacade = cryptoFacade;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public WalletDTO createWallet(CreateWalletRequestDTO dto) {
        log.info("Generating key pair for userId={}", dto.userId());
        // Use the facade to generate the key pair
        EncodedKeyPair keyPair = cryptoFacade.generateKeyPair();
        String publicKey = keyPair.getPublic();
        String privateKey = keyPair.getPrivate();

        // Encrypt the private key using the facade
        String encryptedPrivateKey = cryptoFacade.encryptData(privateKey);

        // 1. Create and save the wallet document (without the private key)
        WalletDocument wallet = new WalletDocument(
                dto.userId(),
                publicKey // The public key is the address
        );
        WalletDocument savedWallet = walletRepository.save(wallet);

        // 2. Create and save the secret document separately
        SecretStorageDocument secret = new SecretStorageDocument(publicKey, encryptedPrivateKey);
        secretStorageRepository.save(secret);

        log.info("Wallet created with ID={} and address={}. Private key stored separately.", savedWallet.getId(), savedWallet.getAddress());

        return WalletMapper.toDTO(savedWallet);
    }

    public WalletDTO getWalletById(String walletId) {
        WalletDocument wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet is not found with ID: " + walletId));
        return WalletMapper.toDTO(wallet);
    }
}