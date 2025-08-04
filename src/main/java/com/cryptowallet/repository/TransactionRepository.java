package com.cryptowallet.repository;

import com.cryptowallet.model.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {
    boolean existsBySignature(String signature);
    Optional<TransactionDocument> findBySignature(String signature);
}
