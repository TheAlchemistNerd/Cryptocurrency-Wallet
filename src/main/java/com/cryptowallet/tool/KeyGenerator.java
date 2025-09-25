package com.cryptowallet.tool;

import com.cryptowallet.crypto.ECKeyPairGenerator;
import com.cryptowallet.crypto.EncodedKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class KeyGenerator {

    public static void main(String[] args) {
        // Ensure Bouncy Castle is registered for EC key generation
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        SecureRandom secureRandom = new SecureRandom();

        // Generate 256-bit (32-byte) key for AES
        byte[] aesKeyBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(aesKeyBytes);
        String base64AesKey = Base64.getEncoder().encodeToString(aesKeyBytes);

        // Generate 256-bit (32-byte) key for JWT (HS256)
        byte[] jwtKeyBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(jwtKeyBytes);
        String base64JwtKey = Base64.getEncoder().encodeToString(jwtKeyBytes);

        // Generate EC key pair
        ECKeyPairGenerator ecKeyPairGenerator = new ECKeyPairGenerator();
        EncodedKeyPair ecKeyPair = ecKeyPairGenerator.generateKeyPair();
        String base64EcPublicKey = ecKeyPair.getPublic();
        String base64EcPrivateKey = ecKeyPair.getPrivate();


        System.out.println("\n--- Generated Keys ---");
        System.out.println("CRYPTO_AES_SECRET=" + base64AesKey);
        System.out.println("JWT_SECRET_KEY=" + base64JwtKey);
        System.out.println("CRYPTO_ECIES_PUBLIC_KEY=" + base64EcPublicKey);
        System.out.println("CRYPTO_ECIES_PRIVATE_KEY=" + base64EcPrivateKey);
    }
}