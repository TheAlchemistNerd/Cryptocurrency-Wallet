package com.cryptowallet.repository;

import com.cryptowallet.model.SecretStorageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretStorageRepository extends MongoRepository<SecretStorageDocument, String> {
}
