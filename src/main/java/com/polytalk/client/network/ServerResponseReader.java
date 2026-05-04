package com.polytalk.client.network;

import com.polytalk.client.service.ClientSecurityService;
import com.polytalk.client.state.ClientState;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 서버 응답을 읽는 클래스
 */
public class ServerResponseReader {

    private final BufferedReader reader;
    private final ClientSecurityService securityService;

    public ServerResponseReader(BufferedReader reader, ClientSecurityService securityService) {
        this.reader = reader;
        this.securityService = securityService;
    }

    /**
     * 원하는 타입의 메시지가 올 때까지 계속 읽기
     */
    public Message readUntil(ClientState state, MessageType... targetTypes) throws Exception {
        while (true) {
            String line;

            try {
                line = reader.readLine(); // 서버 메시지 대기
            } catch (SocketTimeoutException e) {
                // SocketClient setSoTimeout 500 이거 그 부분
                continue;
            }

            if (line == null) {
                throw new IOException("서버 연결이 종료되었습니다.");
            }

            Message msg = JsonUtil.fromJson(line, Message.class);

            // 상대방의 공개키가 오면 자동으로 키를 생성
            if (msg.getType() == MessageType.PUBLIC_KEY) {
                securityService.receivePeerPublicKey(msg, state);
                continue; // 키 처리가 끝나면 다시 다음 메시지 읽으러 감
            }

            // 내가 기다리던 타입의 메시지인지 확인해서 맞으면 반환
            for (MessageType target : targetTypes) {
                if (msg.getType() == target) {
                    return msg;
                }
            }
        }
    }
}