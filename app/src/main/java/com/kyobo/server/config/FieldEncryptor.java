package com.kyobo.server.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 비밀번호 해시(BCrypt)와 필드 암호화(AES-GCM)를 담당한다.
 */
public final class FieldEncryptor {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 앱 고정 salt (ENCRYPTION_SECRET과 함께 키를 유도). 변경 시 기존 암호문은 복호화 불가. */
    private static final byte[] PBKDF2_SALT =
            "kyobo-cinema-field-encrypt-v1".getBytes(StandardCharsets.UTF_8);
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int AES_KEY_BITS = 256;

    private FieldEncryptor() {
    }

    /** 비밀번호용: 복호화 불가능한 해시 생성 */
    public static String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /** 비밀번호용: 평문과 해시 비교 */
    public static boolean matchesPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    /** 이름/전화 등: 복호화 가능한 암호화 */
    public static String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("필드 암호화에 실패했습니다.", e);
        }
    }

    /** 이름/전화 등: 암호문 복호화 */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, aesKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("필드 복호화에 실패했습니다.", e);
        }
    }

    private static SecretKey aesKey() {
        return AesKeyHolder.KEY;
    }

    /** encrypt/decrypt 최초 호출 시에만 키 유도 (hashPassword와 분리) */
    private static final class AesKeyHolder {
        private static final SecretKey KEY = deriveSecretKey();

        private AesKeyHolder() {
        }
    }

    private static SecretKey deriveSecretKey() {
        try {
            KeySpec spec = new PBEKeySpec(
                    Env.encryptionSecret().toCharArray(),
                    PBKDF2_SALT,
                    PBKDF2_ITERATIONS,
                    AES_KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("암호화 키를 만들 수 없습니다.", e);
        }
    }
}
