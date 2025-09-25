package com.cryptowallet.crypto;

/**
 * An abstraction for resolving a key identifier into a decrypted private key.
 * This allows the signing strategy to be decoupled from the key storage mechanism.
 */
public interface PrivateKeyResolver {
    /**
     * Finds and returns a decrypted, Base64-encoded private key for a given identifier.
     * @param keyIdentifier The unique identifier for the key (e.g., wallet address).
     * @return The decrypted, Base64-encoded private key.
     */
    String findDecryptedPrivateKeyByIdentifier(String keyIdentifier);
}
