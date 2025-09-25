package com.cryptowallet.crypto;

/**
 * Defines a strategy for cryptographic signing and verification.
 * Key pair generation is handled by the separate KeyPairGenerator interface.
 */
public interface SignatureStrategy {
    /**
     * Creates a signature for the given data using a key associated with the identifier.
     *
     * @param data The data to sign.
     * @param keyAddressIdentifier The identifier (e.g., wallet address) for the key to use.
     * @return A Base64-encoded signature string.
     */
    String sign(String data, String keyAddressIdentifier);

    boolean verify(String data, String signature, String base64PublicKey);

}
