package com.cryptowallet.config;

import com.cryptowallet.crypto.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;


import static org.assertj.core.api.Assertions.assertThat;

public class CryptoConfigTest {

    private final ApplicationContextRunner contextRunner = new
            ApplicationContextRunner()
            .withUserConfiguration(CryptoConfig.class)
            .withPropertyValues(
                    "CRYPTO_AES_SECRET = 12345678901234567890123456789012"  // 32-byte AES key
            );

    @Test
    void shouldLoadAllCryptoBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EncryptionStrategy.class);
            assertThat(context).hasSingleBean(SignatureStrategy.class);
            assertThat(context).hasSingleBean(EncryptionService.class);
            assertThat(context).hasSingleBean(SignatureService.class);

            EncryptionStrategy encryptionStrategy = context.getBean(EncryptionStrategy.class);
            assertThat(encryptionStrategy).isInstanceOf(AESEncryptionStrategy.class);

            SignatureStrategy signatureStrategy = context.getBean(SignatureStrategy.class);
            assertThat(signatureStrategy).isInstanceOf(ECDSASignatureStrategy.class);
        });
    }

    @Test
    void shouldEncryptAndDecryptUsingFacade() {
        contextRunner.run(context ->  {
            CryptoFacade cryptoFacade = context.getBean(CryptoFacade.class);

            String plaintext = "Test message";
            String encrypted = cryptoFacade.encryptPrivateKey(plaintext);
            String decrypted = cryptoFacade.decryptPrivateKey(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        });
    }

    @Test
    void shouldSignAndVerifyUsingFacade() {
        contextRunner.run(context -> {
            CryptoFacade cryptoFacade = context.getBean(CryptoFacade.class);

            String data = "Important transaction data";
            EncodedKeyPair keyPair = cryptoFacade.generateKeyPair();
            String signature = cryptoFacade.signData(data, keyPair.getPrivate());
            boolean isValid = cryptoFacade.verifySignature(data, signature, keyPair.getPublic());

            assertThat(isValid).isTrue();
        });
    }
}
