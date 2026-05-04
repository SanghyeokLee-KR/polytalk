package com.polytalk.crypto;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * ECDH 키 교환
 */
public class EcdhUtil {

    public static KeyPair generateKeyPair() throws Exception {
        // EC 알고리즘으로 내 공개키/개인키 한 쌍을 만듦
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256); // 256비트 보안 수준 설정
        return generator.generateKeyPair();
    }

    public static byte[] generateSharedSecret(
            PrivateKey myPrivateKey,
            PublicKey otherPublicKey
    ) throws Exception {
        // ECDH 방식으로 키 합의 시작
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(myPrivateKey); // 내 개인키 넣고
        keyAgreement.doPhase(otherPublicKey, true); // 상대방 공개키랑 섞기

        // 섞어서 나온 결과물(비밀값) 반환
        return keyAgreement.generateSecret();
    }

    public static SecretKey generateSecretKey(
            PrivateKey myPrivateKey, // 개인키
            PublicKey otherPublicKey // 공개키
    ) throws Exception {
        // 위에서 만든 비밀값을 가져옴
        byte[] sharedSecret = generateSharedSecret(myPrivateKey, otherPublicKey);

        // AES는 16바이트(128비트) 키가 필요하니까 앞부분만 잘라서 실제 암호키로 변환
        return new SecretKeySpec(sharedSecret, 0, 16, "AES");
    }
}