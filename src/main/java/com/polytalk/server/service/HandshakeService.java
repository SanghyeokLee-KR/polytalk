package com.polytalk.server.service;

import com.polytalk.protocol.Message;
import com.polytalk.server.ClientHandler;

public interface HandshakeService {

    // Cipher Suite 협상
    void negotiateCipherSuite(Message msg, ClientHandler sender);

    // 클라이언트가 보낸 E(SEED)를 받아서 복호화
    void receiveEncryptedSeed(Message msg, ClientHandler sender);

    // 핸드셰이크 완료 여부 확인
    boolean isHandshakeCompleted(ClientHandler sender);

    // 클라이언트 종료 시 핸드셰이크 정보 제거
    void remove(ClientHandler sender);
}