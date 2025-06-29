package com.cryptowallet.repository;

import com.cryptowallet.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    Optional<UserDocument> findByUserName(String userName);
}
