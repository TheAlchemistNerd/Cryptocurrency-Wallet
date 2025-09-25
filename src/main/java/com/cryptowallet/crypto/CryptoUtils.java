package com.cryptowallet.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * A utility class for common cryptographic operations, such as loading keys
 * and ensuring security provider registration. This class cannot be instantiated.
 */
public final class CryptoUtils {

    private static final String KEY_FACTORY_ALGORITHM = "EC";
    public static final String PROVIDER = "BC";

    // Static block to register the Bouncy Castle provider once.
    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // Private constructor to prevent instantiation.
    private CryptoUtils() {}

    /**
     * Loads a PrivateKey from a Base64-encoded string.
     *
     * @param base64PrivateKey The Base64-encoded private key.
     * @return A {@link PrivateKey} object.
     * @throws NoSuchProviderException if the specified provider is not registered.
     * @throws NoSuchAlgorithmException If the EC algorithm is not available.
     * @throws InvalidKeySpecException  If the key specification is invalid.
     */
    public static PrivateKey loadEcPrivateKey(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        byte[] privateBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory factory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM, PROVIDER);
        return factory.generatePrivate(keySpec);
    }

    /**
     * Loads a PublicKey from a Base64-encoded string.
     *
     * @param base64PublicKey The Base64-encoded public key.
     * @return A {@link PublicKey} object.
     * @throws NoSuchProviderException if the specified provider is not registered.
     * @throws NoSuchAlgorithmException If the EC algorithm is not available.
     * @throws InvalidKeySpecException  If the key specification is invalid.
     */
    public static PublicKey loadEcPublicKey(String base64PublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        byte[] publicBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory factory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM, PROVIDER);
        return factory.generatePublic(keySpec);
    }
}
