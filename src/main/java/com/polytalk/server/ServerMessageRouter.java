package com.polytalk.server;

import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.server.service.HandshakeService;
import com.polytalk.server.service.ServerAuthService;
import com.polytalk.server.service.ServerChatService;
import com.polytalk.server.service.ServerRoomService;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지 타입에 따라 서비스를 호출하는 클래스
 */
@Slf4j
public class ServerMessageRouter {

    private final ServerAuthService authService;
    private final ServerRoomService roomService;
    private final ServerChatService chatService;
    private final HandshakeService handshakeService;

    public ServerMessageRouter(
            ServerAuthService authService,
            ServerRoomService roomService,
            ServerChatService chatService,
            HandshakeService handshakeService
    ) {
        this.authService = authService;
        this.roomService = roomService;
        this.chatService = chatService;
        this.handshakeService = handshakeService;
    }

    /**
     * 전달받은 JSON 메시지를 분석해 배분
     */
    public void route(String json, ClientHandler sender) {
        try {
            // 1. JSON 문자열을 Message 객체로 역직렬화
            Message msg = JsonUtil.fromJson(json, Message.class);

            // 2. 메시지 타입에 따른 서비스 호출
            switch (msg.getType()) {

                case SIGN_UP: // 회원가입
                    authService.signUp(msg, sender);
                    break;

                case LOGIN: // 로그인
                    authService.login(msg, sender);
                    break;

                case CIPHER_SUITE_REQUEST: // 보안 협상(핸드쉐이크)
                    handshakeService.negotiateCipherSuite(msg, sender);
                    break;

                case SEED_EXCHANGE: // 암호화 키(시드) 교환
                    handshakeService.receiveEncryptedSeed(msg, sender);
                    break;

                case ROOM_LIST: // 전체 채팅방 목록 요청
                    roomService.sendRoomList(sender);
                    break;

                case ROOM_CREATE: // 채팅방 생성
                    roomService.createRoom(msg, sender);
                    break;

                case ROOM_JOIN: // 채팅방 입장
                    roomService.joinRoom(msg, sender);
                    break;

                case ROOM_LEAVE: // 채팅방 퇴장
                    roomService.leaveRoom(msg, sender);
                    break;

                case ROOM_DESTROY: // 채팅방 제거
                    roomService.destroyRoom(msg);
                    break;

                case CHAT: // 일반 채팅 메시지 전송
                    // 보안을 위해 핸드쉐이크가 끝난 사용자만 허용함
                    if (!handshakeService.isHandshakeCompleted(sender)) {
                        return;
                    }
                    chatService.handleChat(msg, sender);
                    break;

                case LOGOUT: // 로그아웃 및 보안 세션 정보 제거
                    authService.logout(msg, sender);
                    handshakeService.remove(sender);
                    break;

                default: // 정의되지 않은 메시지 타입은 처리하지 않음
                    break;
            }

        } catch (Exception e) {
            log.error("메시지 처리 오류", e);
        }
    }
}