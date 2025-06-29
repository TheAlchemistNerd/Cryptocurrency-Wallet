package com.cryptowallet.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDSASignatureStrategy implements SignatureStrategy {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGO = "SHA256withECDSA";
    private static final String CURVE = "EC";

    private static String PROVIDER = "BC";

    private static final String CURVE_SPEC = "secp256k1";

    @Override
    public EncodedKeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CURVE, PROVIDER);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_SPEC); // Named curve
            generator.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(
                    keyPair.getPublic().getEncoded()
            );
            String privateKey = Base64.getEncoder().encodeToString(
                    keyPair.getPrivate().getEncoded()
            );

            return new EncodedKeyPair(publicKey, privateKey);

        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }

    @Override
    public String sign(String data, String base64PrivateKey) {
        try {
            byte[] privateBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory factory = KeyFactory.getInstance(CURVE, PROVIDER);
            PrivateKey privateKey = factory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance(ALGO, PROVIDER);
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
            byte[] publicBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory factory = KeyFactory.getInstance(CURVE, PROVIDER);
            PublicKey publicKey = factory.generatePublic(keySpec);

            Signature signature = Signature.getInstance(ALGO, PROVIDER);
            signature.initVerify(publicKey);
            byte[] sigBytes = Base64.getDecoder().decode(signatureStr);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(sigBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
