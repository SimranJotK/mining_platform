package com.cryptomining.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 65536;

    private final String masterKey;

    public EncryptionService(@Value("${app.encryption.master-key}") String masterKey) {
        this.masterKey = masterKey;
    }

    public EncryptionResult encryptWithUserKey(String plaintext, String userPassphrase, String salt) {
        try {
            SecretKey key = deriveKey(userPassphrase, salt);
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            return new EncryptionResult(encrypted, Base64.getEncoder().encodeToString(iv), salt);
        } catch (Exception e) {
            throw new SecurityException("Encryption failed", e);
        }
    }

    public String decryptWithUserKey(byte[] ciphertext, String iv, String userPassphrase, String salt) {
        try {
            SecretKey key = deriveKey(userPassphrase, salt);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key,
                new GCMParameterSpec(GCM_TAG_LENGTH, Base64.getDecoder().decode(iv)));
            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new SecurityException("Decryption failed - invalid key or corrupted data", e);
        }
    }

    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public PublicKey decodePublicKey(String encoded) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(encoded);
        return KeyFactory.getInstance("RSA")
            .generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public byte[] encryptApiKey(String apiKey, Long userId) {
        try {
            SecretKey key = deriveMasterKey(userId.toString());
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(apiKey.getBytes());
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return combined;
        } catch (Exception e) {
            throw new SecurityException("API key encryption failed", e);
        }
    }

    private SecretKey deriveKey(String passphrase, String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(),
            Base64.getDecoder().decode(salt), PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private SecretKey deriveMasterKey(String context) throws Exception {
        String salt = Base64.getEncoder().encodeToString(context.getBytes());
        return deriveKey(masterKey, salt);
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public record EncryptionResult(byte[] encryptedPayload, String iv, String salt) {}
}
