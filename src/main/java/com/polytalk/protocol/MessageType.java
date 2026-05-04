package com.polytalk.protocol;

/**
 * 통신 메시지의 유형을 정의하는 열거형
 */
public enum MessageType {
    // --- 회원가입 ---
    SIGN_UP,            // 회원가입 요청
    SIGN_UP_SUCCESS,    // 회원가입 성공
    SIGN_UP_FAIL,       // 회원가입 실패

    // --- 로그인 ---
    LOGIN,              // 로그인 요청
    LOGIN_SUCCESS,      // 로그인 성공
    LOGIN_FAIL,         // 로그인 실패

    // --- 보안 핸드셰이크 ---
    CIPHER_SUITE_REQUEST,  // 암호화 방식 제안 요청
    CIPHER_SUITE_RESPONSE, // 암호화 방식 결정 응답
    CIPHER_SUITE_FAIL,     // 보안 협상 실패

    SEED_EXCHANGE,      // 시드 교환
    HANDSHAKE_SUCCESS,  // 보안 연결 성공
    HANDSHAKE_FAIL,     // 보안 연결 실패

    PUBLIC_KEY,         // 공개키 전달

    // --- 채팅방 관리 ---
    ROOM_LIST,          // 방 목록 요청
    ROOM_LIST_RESULT,   // 방 목록 결과 전달

    ROOM_CREATE,         // 방 생성 요청
    ROOM_CREATE_SUCCESS, // 방 생성 성공
    ROOM_CREATE_FAIL,    // 방 생성 실패

    ROOM_JOIN,           // 방 입장 요청
    ROOM_JOIN_SUCCESS,   // 방 입장 성공
    ROOM_JOIN_FAIL,      // 방 입장 실패

    ROOM_LEAVE,          // 방 나가기

    // --- 대화 및 기록 ---
    CHAT_HISTORY,       // 이전 채팅 기록
    CHAT,               // 메세지

    // --- 방 파기 ---
    ROOM_DESTROY,       // 방 폭파
    ROOM_DESTROYED,     // 방 폭파 알림

    // --- 종료 ---
    LOGOUT,
}