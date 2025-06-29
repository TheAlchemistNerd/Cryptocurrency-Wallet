package com.cryptowallet.repository;

import com.cryptowallet.model.WalletDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WalletRepository extends MongoRepository<WalletDocument, String> {
    Optional<WalletDocument> findByAddress(String address);
}
