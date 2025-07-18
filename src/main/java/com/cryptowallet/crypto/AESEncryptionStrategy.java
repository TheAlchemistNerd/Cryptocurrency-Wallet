package com.cryptowallet.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryptionStrategy implements EncryptionStrategy {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits is recommended for GCM
    private static final int TAG_LENGTH = 128;  // 128 bits for the authentication tag

    private final SecretKey key;

    public AESEncryptionStrategy(String secret) {
        // Decode the Base64 secret string to get the original key bytes
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length != 32) throw new IllegalArgumentException("AES key must be exactly 32 bytes (256 bits)");
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String encrypt(String data) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv); // generate new IV per encryption
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherBytes = new byte[decoded.length - IV_LENGTH];

            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, cipherBytes, 0, cipherBytes.length);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(cipherBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
