package com.cryptowallet.tool;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {

    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();

        // Generate 256-bit (32-byte) key for AES
        byte[] aesKeyBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(aesKeyBytes);
        String base64AesKey = Base64.getEncoder().encodeToString(aesKeyBytes);

        // Verification check for the generated Base64 key
        byte[] decodedAesBytes = Base64.getDecoder().decode(base64AesKey);
        System.out.println("Generated Base64 AES Key: " + base64AesKey);
        System.out.println("Decoded AES Key byte length: " + decodedAesBytes.length);
        if (decodedAesBytes.length != 32) {
            System.err.println("WARNING: Decoded AES key is NOT 32 bytes! Something is wrong with Base64 encoding/decoding logic.");
        }

        // Generate 256-bit (32-byte) key for JWT (HS256)
        byte[] jwtKeyBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(jwtKeyBytes);
        String base64JwtKey = Base64.getEncoder().encodeToString(jwtKeyBytes);

        // Verification check for the generated Base64 JWT key
        byte[] decodedJwtBytes = Base64.getDecoder().decode(base64JwtKey);
        System.out.println("Generated Base64 JWT Key: " + base64JwtKey);
        System.out.println("Decoded JWT Key byte length: " + decodedJwtBytes.length);
        if (decodedJwtBytes.length != 32) {
            System.err.println("WARNING: Decoded JWT key is NOT 32 bytes! Something is wrong with Base64 encoding/decoding logic.");
        }

        System.out.println("\n--- Copy these values ---");
        System.out.println("CRYPTO_AES_SECRET=" + base64AesKey);
        System.out.println("JWT_SECRET_KEY=" + base64JwtKey);
    }
}
        