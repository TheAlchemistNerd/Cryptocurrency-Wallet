package com.cryptowallet.repository;

import com.cryptowallet.model.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {
    List<TransactionDocument> findBySenderAddress(String senderAddress);
}
