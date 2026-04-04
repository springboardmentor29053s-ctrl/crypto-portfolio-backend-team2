package com.crypto_portfolio_tracker.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class CryptoUtil {

    private static final String Algorithm = "AES/CBC/PKCS5Padding";
    private static final byte[] FIXED_IV = "DefaultInitVector".getBytes(); // fallback IV for old data

    @Value("${crypto.secretkey}")
    private String cryptoSecretKey;

    /** Encrypts data with random IV prepended */
    public String encrypt(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Data to encrypt cannot be null or blank");
        }
        try {
            Cipher cipher = Cipher.getInstance(Algorithm);

            log.info("CRYPTO KEY = {}", cryptoSecretKey);

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Key keySpec = new SecretKeySpec(cryptoSecretKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());

            // prepend IV
            byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.getMessage(), e);
        }
    }

    /** Decrypts data; handles new (IV prepended) and old (no IV) formats */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isBlank()) {
            throw new IllegalArgumentException("Data to decrypt cannot be null or blank");
        }

        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);

            byte[] iv;
            byte[] encrypted;

            if (encryptedWithIv.length > 16) {
                // new format: first 16 bytes = IV
                iv = new byte[16];
                encrypted = new byte[encryptedWithIv.length - 16];
                System.arraycopy(encryptedWithIv, 0, iv, 0, 16);
                System.arraycopy(encryptedWithIv, 16, encrypted, 0, encrypted.length);
            } else {
                // old format: fallback to fixed IV
                iv = FIXED_IV;
                encrypted = encryptedWithIv;
            }
            Key keySpec = new SecretKeySpec(cryptoSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(Algorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted));
        } catch (IllegalArgumentException e) {
            // Not Base64 → assume already plain
            return encryptedData;
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.getMessage(), e);
        }
    }
}