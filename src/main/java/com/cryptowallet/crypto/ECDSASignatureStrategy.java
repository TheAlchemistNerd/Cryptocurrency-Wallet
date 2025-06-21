package com.cryptowallet.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class ECDSASignatureStrategy implements SignatureStrategy {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGO = "SHA256withECDSA";
    private static final String CURVE = "EC";

    @Override
    public EncodedKeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CURVE, "BC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();

            String publicKeyEncoded = Base64.getEncoder().encodeToString(
                    keyPair.getPublic().getEncoded()
            );
            String privateKeyEncoded = Base64.getEncoder().encodeToString(
                    keyPair.getPrivate().getEncoded()
            );

            return new EncodedKeyPair(publicKeyEncoded, privateKeyEncoded);

        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }

    @Override
    public String sign(String data, String base64PrivateKey) {
        try{
            byte[] privateBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory factory = KeyFactory.getInstance(CURVE, "BC");
            PrivateKey privateKey = factory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance(ALGO, "BC");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("Signing failed");
        }
    }

    @Override
    public boolean verify(String data, String signatureStr, String base64PublicKey) {
        try {
            byte[] publicBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory factory = KeyFactory.getInstance(CURVE, "BC");
            PublicKey publicKey = factory.generatePublic(keySpec);

            Signature signature = Signature.getInstance(ALGO, "BC");
            signature.initVerify(publicKey);
            byte[] sigBytes = Base64.getDecoder().decode(signatureStr);
            return signature.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
