package com.cryptowallet.config;

import com.cryptowallet.merkle.MerkleTreeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class for blockchain-related components.
 * This class explicitly defines beans for components like MerkleTreeBuilder.
 */
@Configuration
public class BlockChainConfig {

    /**
     * Defines a MerkleTreeBuilder bean.
     * @return A new instance of MerkleTreeBuilder.
     */
    @Bean
    public MerkleTreeBuilder merkleTreeBuilder() {
        return new MerkleTreeBuilder();
    }
}