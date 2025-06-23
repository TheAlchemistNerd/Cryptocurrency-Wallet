package com.cryptowallet.repository;

import com.cryptowallet.model.WalletDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WalletRepository extends MongoRepository<WalletDocument, String> {
    List<WalletDocument> findByUserId(String userId);
}
