package com.cryptowallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CryptoWalletApplication {
    public static void main(String[] args) {
        SpringApplication.run(CryptoWalletApplication.class, args);
    }
}
