package com.polytalk.crypto;

import java.security.MessageDigest;
import java.security.PublicKey;

/**
 * 공개키 지문을 만들어 주는 유틸 클래스
 * 유저 확인용
 */
public class FingerprintUtil {

    public static String fingerprint(PublicKey publicKey) {
        try {
            // 1. 공개키 데이터를 SHA-256으로 해싱
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(publicKey.getEncoded());

            StringBuilder sb = new StringBuilder();

            // 2. 앞 16바이트만 16진수로 변환 (ex: AA:BB:CC...)
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02X", hash[i]));

                if (i < 15) {
                    sb.append(":");
                }
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("공개키 지문 생성 실패", e);
        }
    }

    /**
     * Base64 문자열로 된 공개키를 넣으면 지문 나옴
     */
    public static String fingerprintFromBase64(String base64PublicKey) {
        try {
            PublicKey publicKey = KeyUtil.base64ToPublicKey(base64PublicKey);
            return fingerprint(publicKey);

        } catch (Exception e) {
            throw new RuntimeException("공개키 지문 변환 실패", e);
        }
    }
}