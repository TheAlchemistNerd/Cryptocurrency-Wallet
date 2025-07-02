package com.cryptowallet.merkle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Abstract base class for Merkle Tree nodes (Composite Pattern - Component).
 * Provides common functionality for hashing.
 */
public abstract class MerkleNode {
    protected String hash;

    /**
     * Returns the cryptographic hash for this node
     * @return The SHA-256 hash as a Base64 encoded string.
     */
    public abstract String getHash();

    /**
     * Calculate the SHA-256 hash of a given text
     * @param text the input text to hash.
     * @return The SHA-256 hash as a Base64 encoded string.
     * @throws RuntimeException if SHA-256 algorithm is not available (highly unlikely)
     */
    protected static String calculateSha256Hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }
}
