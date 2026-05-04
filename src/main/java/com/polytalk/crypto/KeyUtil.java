package com.polytalk.crypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 키(Key) 객체와 문자열/바이트 간의 변환을 담당
 */
public class KeyUtil {

    // 공개키를 JSON 등에 담아 보내기 위해 Base64 문자열로 변환
    public static String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // 받은 Base64 문자열을 다시 공개키 객체로 복원
    public static PublicKey base64ToPublicKey(String base64PublicKey) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64PublicKey);
        return bytesToPublicKey(decoded);
    }

    // 바이트 데이터를 공개키로 변환
    public static PublicKey bytesToPublicKey(byte[] bytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
    }

    // 바이트 데이터를 개인키로 변환
    public static PrivateKey bytesToPrivateKey(byte[] bytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
}