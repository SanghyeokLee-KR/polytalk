package com.polytalk.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES/GCM 암호화 유틸
 */
public class AesGcmUtil {

    // GCM 인증 태그 길이 (데이터 변조 체크용)
    private static final int GCM_TAG_LENGTH = 128;
    // GCM 표준 IV 길이 (12바이트)
    private static final int IV_LENGTH = 12;

    /**
     * 128비트 AES 키 생성
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    /**
     * 문자열 암호화
     */
    public static String encrypt(String plainText, SecretKey key) throws Exception {
        return encrypt(plainText.getBytes(StandardCharsets.UTF_8), key);
    }

    /**
     * 바이트 배열 암호화
     */
    public static String encrypt(byte[] plainBytes, SecretKey key) throws Exception {
        // 1. IV(초기화 벡터) 생성: 같은 비번이라도 암호문이 매번 다르게 나오게 함
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encrypted = cipher.doFinal(plainBytes);

        // 2. 복호화할 때 IV가 꼭 필요해서 [IV : 암호문] 형태로 합쳐서 보냄
        return Base64.getEncoder().encodeToString(iv) + ":" +
                Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 암호문 복호화 (다시 읽을 수 있는 텍스트로)
     */
    public static String decrypt(String encryptedText, SecretKey key) throws Exception {
        byte[] decrypted = decryptToBytes(encryptedText, key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 암호문 복호화 (바이트 배열로)
     */
    public static byte[] decryptToBytes(String encryptedText, SecretKey key) throws Exception {
        // 1. 아까 합쳤던 [IV : 암호문]을 ':' 기준으로 다시 쪼갬
        String[] parts = encryptedText.split(":", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("AES/GCM 암호문 형식이 올바르지 않습니다.");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encrypted = Base64.getDecoder().decode(parts[1]);

        // 2. 쪼갠 IV랑 키를 가지고 복호화 진행
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return cipher.doFinal(encrypted);
    }
}