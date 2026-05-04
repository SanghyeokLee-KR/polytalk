package com.polytalk.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 시드(Seed) 값을 가공해서 실제 AES 암호화 키를 만드는 유틸리티
 */
public class KeyDerivationUtil {

    private static final int AES_KEY_BIT_LENGTH = 128; // AES - 128비트

    private KeyDerivationUtil() {}

    /**
     * 시드와 반복 횟수를 받아서 AES 키 생성
     */
    public static SecretKey deriveAesKey(byte[] seed, int iterationCount) {
        try {
            // 바이트 배열인 시드를 문자열로 변환
            String seedText = Base64.getEncoder().encodeToString(seed);

            // PBKDF2 설정: [시드, 솔트, 반복 횟수, 키 길이]
            PBEKeySpec spec = new PBEKeySpec(
                    seedText.toCharArray(),
                    seed,
                    iterationCount, // 반복 수
                    AES_KEY_BIT_LENGTH
            );

            // HMAC-SHA256 기반의 PBKDF2 알고리즘 사용
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            // 설정대로 Factory 돌려서 키 바이트 추출
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();

            // 추출된 바이트를 최종 AES 키 객체로 변환
            return new SecretKeySpec(keyBytes, "AES");

        } catch (Exception e) {
            throw new RuntimeException("SecretKey 유도 실패", e);
        }
    }
}