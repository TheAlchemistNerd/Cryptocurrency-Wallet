package com.cryptowallet.crypto;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

@Component
public class ECDSASignatureStrategy implements SignatureStrategy {

    private static final String ALGORITHM = "SHA256withECDSA";

    private final PrivateKeyResolver privateKeyResolver;

    public ECDSASignatureStrategy(PrivateKeyResolver privateKeyResolver) {
        this.privateKeyResolver = privateKeyResolver;
    }

    @Override
    public String sign(String data, String keyAddressIdentifier) {
        try {
            // 1. Resolve the decrypted private key using the abstraction
            String decryptedPrivateKey = privateKeyResolver.findDecryptedPrivateKeyByIdentifier(keyAddressIdentifier);

            // 2. Load the private key object and sign
            PrivateKey privateKey = CryptoUtils.loadEcPrivateKey(decryptedPrivateKey);
            Signature signature = Signature.getInstance(ALGORITHM, CryptoUtils.PROVIDER);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }

    @Override
    public boolean verify(String data, String signatureStr, String base64PublicKey) {
        try {
            PublicKey publicKey = CryptoUtils.loadEcPublicKey(base64PublicKey);
            Signature signature = Signature.getInstance(ALGORITHM, CryptoUtils.PROVIDER);
            signature.initVerify(publicKey);
            byte[] sigBytes = Base64.getDecoder().decode(signatureStr);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }
}