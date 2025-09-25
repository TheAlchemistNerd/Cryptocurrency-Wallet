package com.cryptowallet.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * A dedicated key-pair generator for secp256k1 Elliptic Curve keys.
 */
public class ECKeyPairGenerator implements com.cryptowallet.crypto.KeyPairGenerator {

    private static final String CURVE = "EC";
    private static final String CURVE_SPEC = "secp256k1";

    @Override
    public EncodedKeyPair generateKeyPair() {
        try {
            // CryptoUtils ensures the Bouncy Castle provider is registered.
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CURVE, CryptoUtils.PROVIDER);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_SPEC);
            generator.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return new EncodedKeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }
}
