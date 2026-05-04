package com.polytalk.crypto;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 서버와 클라이언트가 사용할 암호화 방식 정의
 */
@Getter
public enum CipherSuite {

    // ECDH 키 교환 + AES/GCM 암호화 + SHA-256 기반 키 유도
    ECDH_AES_GCM_SHA256("ECDH_AES_GCM_SHA256");

    private final String name;

    CipherSuite(String name) {
        this.name = name;
    }

    /**
     * 문자열 이름과 일치하는 Enum 항목을 찾아줌
     * (없을 수도 있으니 Optional 사용)
     */
    public static Optional<CipherSuite> fromName(String name) {
        return Arrays.stream(values())
                .filter(suite -> suite.name.equals(name))
                .findFirst();
    }

    // 현재 서버의 암호화 방식의 이름 리스트를 반환
    public static List<String> supportedNames() {
        return Arrays.stream(values())
                .map(CipherSuite::getName)
                .toList();
    }
}