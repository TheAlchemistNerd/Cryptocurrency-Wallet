package com.cryptowallet.repository;

import com.cryptowallet.model.Block;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends MongoRepository<Block, String> {
    Optional<Block> findTopByOrderByTimestampDesc();
}
