package com.polytalk.crypto;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 내 키 쌍(공개키, 개인키)을 파일(.key)로 저장하고 관리
 */
public class KeyStoreUtil {

    private static final String DIR = "client_keys"; // 키 저장 폴더명

    // 내 키 쌍을 파일로 저장 (로그인 세션 유지용)
    public static void saveKeyPair(String userId, KeyPair keyPair) {
        try {
            File dir = new File(DIR);
            dir.mkdirs(); // 폴더 없으면 생성

            // 개인키 저장 (내거)
            Files.write(new File(dir, userId + "_private.key").toPath(),
                    keyPair.getPrivate().getEncoded());

            // 공개키 저장 (상대방 줄 것)
            Files.write(new File(dir, userId + "_public.key").toPath(),
                    keyPair.getPublic().getEncoded());

        } catch (Exception e) {
            throw new RuntimeException("키 저장 실패", e);
        }
    }

    // 파일에서 저장된 내 키 쌍을 읽어옴
    public static KeyPair loadKeyPair(String userId) {
        try {
            File privateFile = new File(DIR, userId + "_private.key");
            File publicFile = new File(DIR, userId + "_public.key");

            // 저장된 파일이 없으면 null 반환 (새로 생성해야 함)
            if (!privateFile.exists() || !publicFile.exists()) {
                return null;
            }

            // 파일 바이트를 읽어서 KeyUtil을 통해 객체로 복원
            PrivateKey privateKey = KeyUtil.bytesToPrivateKey(
                    Files.readAllBytes(privateFile.toPath())
            );

            PublicKey publicKey = KeyUtil.bytesToPublicKey(
                    Files.readAllBytes(publicFile.toPath())
            );

            return new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            throw new RuntimeException("키 불러오기 실패", e);
        }
    }
}