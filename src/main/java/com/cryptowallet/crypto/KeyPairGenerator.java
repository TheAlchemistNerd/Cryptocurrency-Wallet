package com.cryptowallet.crypto;

/**
 * Defines a strategy for generating cryptographic key pairs.
 * This is separated from SignatureStrategy to adhere to the Interface Segregation Principle.
 */
public interface KeyPairGenerator {
    EncodedKeyPair generateKeyPair();
}
