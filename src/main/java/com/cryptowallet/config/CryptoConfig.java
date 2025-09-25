package com.cryptowallet.config;

import com.cryptowallet.crypto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

    // --- Encryption Beans ---
    @Bean
    @Qualifier
    public EncryptionStrategy eciesEncryptionStrategy(
            @Value("${CRYPTO_ECIES_PUBLIC_KEY:}") String publicKey,
            @Value("${CRYPTO_ECIES_PRIVATE_KEY:}") String privateKey
    ) {
        return new ECIESEncryptionStrategy(publicKey, privateKey);
    }

    @Bean
    public EncryptionService encryptionService(EncryptionStrategy eciesEncryptionStrategy) {
        return new EncryptionService(eciesEncryptionStrategy);
    }

    // --- KeyPair Generation Beans (Internal) ---
    @Bean
    public KeyPairGenerator keyPairGenerator() {
        return new ECKeyPairGenerator();
    }

    @Bean
    public KeyPairService keyPairService(KeyPairGenerator keyPairGenerator) {
        return new KeyPairService(keyPairGenerator);
    }

    // --- Signature Beans ---
    @Bean
    public SignatureStrategy signatureStrategy(PrivateKeyResolver privateKeyResolver) {
        return new ECDSASignatureStrategy(privateKeyResolver);
    }

    @Bean
    public SignatureService signatureService(SignatureStrategy signatureStrategy) {
        return new SignatureService(signatureStrategy);
    }

    // --- Main Facade ---
    @Bean
    public CryptoFacade cryptoFacade(EncryptionService encryptionService, SignatureService signatureService, KeyPairService keyPairService) {
        return new CryptoFacade(encryptionService, signatureService, keyPairService);
    }
}