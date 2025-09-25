package com.cryptowallet.config;

import com.cryptowallet.crypto.*;
import com.cryptowallet.service.WalletPrivateKeyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CryptoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CryptoConfig.class)
            // Provide a mock bean for the PrivateKeyResolver dependency
            .withBean(WalletPrivateKeyResolver.class, () -> mock(WalletPrivateKeyResolver.class))
            .withPropertyValues(
                    "CRYPTO_ECIES_PUBLIC_KEY=MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEXeQtCP3r4EpSurhW2k1QQMDXe6+L875A1DlVYKH/3y8P+aNPkQFX00ulfOZJA4AC1KFrCYELZyHDD80UOG6GSA==",
                    "CRYPTO_ECIES_PRIVATE_KEY=MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgzGeGUwuReP6rg8t3ebVeGTP8zCKMrlxsH6Y6+EWc9uagBwYFK4EEAAqhRANCAARd5C0I/evgSlK6uFbaTVBAwNd7r4vzvkDUOVVgof/fLw/5o0+RAVfTS6V85kkDgALUoWsJgQtnIcMPzRQ4boZI"
            );

    @Test
    void shouldLoadAllCryptoBeans() {
        contextRunner.run(context -> {
            // Verify that all the new crypto beans are correctly loaded
            assertThat(context).hasSingleBean(EncryptionStrategy.class);
            assertThat(context).hasSingleBean(EncryptionService.class);
            assertThat(context).hasSingleBean(KeyPairGenerator.class);
            assertThat(context).hasSingleBean(KeyPairService.class);
            assertThat(context).hasSingleBean(SignatureStrategy.class);
            assertThat(context).hasSingleBean(SignatureService.class);
            assertThat(context).hasSingleBean(CryptoFacade.class);

            // Verify that the correct strategies are wired
            assertThat(context.getBean(EncryptionStrategy.class)).isInstanceOf(ECIESEncryptionStrategy.class);
            assertThat(context.getBean(KeyPairGenerator.class)).isInstanceOf(ECKeyPairGenerator.class);
            assertThat(context.getBean(SignatureStrategy.class)).isInstanceOf(ECDSASignatureStrategy.class);
        });
    }
}
