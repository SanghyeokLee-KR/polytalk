package com.polytalk.crypto;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt 라이브러리 사용
 */
public class PasswordUtil {

    // 비밀번호 해싱 salt 적용
    public static String hash(String plainPassword) {

        // gensalt() -> 소금
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // 비밀번호 대조
    public static boolean matches(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
