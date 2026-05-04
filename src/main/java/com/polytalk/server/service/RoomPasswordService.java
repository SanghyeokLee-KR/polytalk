package com.polytalk.server.service;

import com.polytalk.crypto.PasswordUtil;

/**
 * 채팅방 비밀번호 서비스
 */
public class RoomPasswordService {

    // 채팅방 만들 때 입력한 비번을 해시로 변환해서 저장용으로 만듦
    public String hash(String rawPassword) {
        return PasswordUtil.hash(rawPassword);
    }

    // 방 입장 시 입력한 비번이 저장된 해시와 맞는지 확인
    public boolean matches(String rawPassword, String hashedPassword) {
        if (isBlank(rawPassword)) return false;
        if (isBlank(hashedPassword)) return false;

        return PasswordUtil.matches(rawPassword, hashedPassword);
    }

    // 공백 ㅔㅊ크
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}