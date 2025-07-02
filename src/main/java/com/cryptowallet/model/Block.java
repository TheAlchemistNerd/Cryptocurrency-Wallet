package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a Block in the conceptual blockchain, encapsulating a list of transaction IDs,
 * a timestamp, the Merkle Root of its transactions, and hashes for chaining.
 */
@Document("blocks")
public class Block {
    @Id
    private String id; // MongoDB ID

    private List<String> transactionsIds; //Store IDs instead of full TransactionDocuments to save space
    private Instant timestamp;
    private String merkleRoot;
    private String previousBlockHash; // Hash of the previous block on the chain
    private String currentBlockHash; // Hash of this block

    public Block() {
        this.timestamp = Instant.now();
    }

    public Block(List<String> transactionsIds, String merkleRoot, String previousBlockHash) {
        this.transactionsIds = transactionsIds;
        this.timestamp = Instant.now();  // Set timestamp upon creation
        this.merkleRoot = merkleRoot;
        this.previousBlockHash = previousBlockHash;
        this.currentBlockHash = calculateHash(); // Calculate the hash immediately upon creation
    }

    /**
     * Calculates the SHA-256 hash of this block.
     * The hash is based on the timestamp, Merkle root, previous block hash, and concatenated transaction IDs.
     * @return The SHA-256 hash of the block as a Base64 encoded string.
     */
    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder dataToHash = new StringBuilder();
            dataToHash.append(timestamp.toString());
            dataToHash.append(merkleRoot);
            dataToHash.append(previousBlockHash);
            // Ensure transaction IDs are consistently ordered for hashing
            String sortedTransactionIds = transactionsIds.stream().sorted().collect(Collectors.joining(","));
            dataToHash.append(sortedTransactionIds);

            byte[] hashBytes = digest.digest(dataToHash.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public List<String> getTransactionsIds() {
        return transactionsIds;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public String getCurrentBlockHash() {
        return currentBlockHash;
    }

    // Setters (primarily for MongoDB to hydrate the object)

    public void setId(String id) {
        this.id = id;
    }

    public void setTransactionsIds(List<String> transactionsIds) {
        this.transactionsIds = transactionsIds;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public void setCurrentBlockHash(String currentBlockHash) {
        this.currentBlockHash = currentBlockHash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Block block = (Block) obj;
        return Objects.equals(id, block.id) &&
                Objects.equals(transactionsIds, block.transactionsIds) &&
                Objects.equals(timestamp, block.timestamp) &&
                Objects.equals(merkleRoot, block.merkleRoot) &&
                Objects.equals(previousBlockHash, block.previousBlockHash) &&
                Objects.equals(currentBlockHash, block.currentBlockHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionsIds, timestamp, merkleRoot, previousBlockHash,currentBlockHash);
    }
}
