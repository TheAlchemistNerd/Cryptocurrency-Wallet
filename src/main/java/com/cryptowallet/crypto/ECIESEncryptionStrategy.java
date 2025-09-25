package com.cryptowallet.crypto;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * An implementation of EncryptionStrategy using the Elliptic Curve Integrated Encryption Scheme (ECIES)
 * with the secp256k1 curve, leveraging the Bouncy Castle provider.
 */
public class ECIESEncryptionStrategy implements EncryptionStrategy {

    private static final String ALGORITHM = "ECIES";

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    /**
     * Constructs the ECIES strategy using a Base64-encoded public and/or private key.
     * The Bouncy Castle provider is registered via the CryptoUtils class.
     *
     * @param b64PublicKey  The Base64-encoded public key for encryption. Can be null if only decrypting.
     * @param b64PrivateKey The Base64-encoded private key for decryption. Can be null if only encrypting.
     */
    public ECIESEncryptionStrategy(String b64PublicKey, String b64PrivateKey) {
        try {
            this.publicKey = (b64PublicKey != null) ? CryptoUtils.loadEcPublicKey(b64PublicKey) : null;
            this.privateKey = (b64PrivateKey != null) ? CryptoUtils.loadEcPrivateKey(b64PrivateKey) : null;
        } catch (Exception e) {
            // Wrap checked exceptions from key loading into a runtime exception
            throw new IllegalArgumentException("Failed to load EC key pair", e);
        }
    }

    @Override
    public String encrypt(String data) {
        if (publicKey == null) {
            throw new IllegalStateException("Public key is not available. Cannot perform encryption.");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, CryptoUtils.PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("ECIES encryption error", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (privateKey == null) {
            throw new IllegalStateException("Private key is not available. Cannot perform decryption.");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, CryptoUtils.PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("ECIES decryption error", e);
        }
    }
}
