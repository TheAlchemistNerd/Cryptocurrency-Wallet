package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.crypto.PrivateKeyResolver;
import com.cryptowallet.exception.WalletNotFoundException;
import com.cryptowallet.model.SecretStorageDocument;
import com.cryptowallet.repository.SecretStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Implementation of PrivateKeyResolver that retrieves an encrypted key
 * from the dedicated secret storage and uses the CryptoFacade to decrypt it.
 */
@Service
public class WalletPrivateKeyResolver implements PrivateKeyResolver {

    private final SecretStorageRepository secretStorageRepository;
    private final CryptoFacade cryptoFacade;

    @Autowired
    public WalletPrivateKeyResolver(SecretStorageRepository secretStorageRepository, @Lazy CryptoFacade cryptoFacade) {
        this.secretStorageRepository = secretStorageRepository;
        this.cryptoFacade = cryptoFacade;
    }

    @Override
    public String findDecryptedPrivateKeyByIdentifier(String keyIdentifier) {
        SecretStorageDocument secret = secretStorageRepository.findById(keyIdentifier)
                  .orElseThrow(() -> new WalletNotFoundException("No private key found for identifier: " + keyIdentifier));

        return cryptoFacade.decryptData(secret.getEncryptedPrivateKey());
    }
}
