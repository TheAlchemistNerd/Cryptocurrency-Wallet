package com.cryptowallet.config;

import com.cryptowallet.crypto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

    @Value("${crypto.aes.secret}")
    private String aesSecret;

    // 🔐 Strategy Beans
    @Bean
    public EncryptionStrategy encryptionStrategy() {
        return new AESEncryptionStrategy(aesSecret);
    }

    @Bean
    public SignatureStrategy signatureStrategy() {
        return new ECDSASignatureStrategy();
    }

    // 💼 Service Beans (decoupled from concrete strategies)
    @Bean
    public EncryptionService encryptionService(EncryptionStrategy encryptionStrategy) {
        return new EncryptionService(encryptionStrategy);
    }

    @Bean
    public SignatureService signatureService(SignatureStrategy signatureStrategy) {
        return new SignatureService(signatureStrategy);
    }

    @Bean
    public CryptoFacade cryptoFacade(EncryptionService encryptionService, SignatureService signatureService) {
        return new CryptoFacade(encryptionService, signatureService);
    }
}
