package com.polytalk.server.service;

import com.polytalk.controller.MemberController;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;
import com.polytalk.server.ClientHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 서버에서 회원가입, 로그인, 로그아웃을 처리하는 서비스
 */
@Slf4j
public class ServerAuthService {

    private final MemberController memberController;
    private final PublicKeyService publicKeyService;

    public ServerAuthService(
            MemberController memberController,
            PublicKeyService publicKeyService
    ) {
        this.memberController = memberController;
        this.publicKeyService = publicKeyService;
    }

    public void signUp(Message msg, ClientHandler sender) {
        // 데이터에서 비번이랑 공개키 분리 (형식 안 맞으면 컷)
        String[] parts = splitPasswordAndPublicKey(msg.getData());

        if (parts == null) {
            log.warn("회원가입 실패 userId={}", msg.getSender());
            send(sender, MessageType.SIGN_UP_FAIL, "회원가입 실패", null);
            return;
        }

        boolean success = memberController.signUp(
                msg.getSender(),
                parts[0],
                parts[1]
        );

        // 삼항연산자 사용
        send(sender,
                success ? MessageType.SIGN_UP_SUCCESS : MessageType.SIGN_UP_FAIL,
                success ? "회원가입 성공" : "회원가입 실패",
                null);

        log.info("회원가입 처리. userId={}, success={}", msg.getSender(), success);
    }

    public void login(Message msg, ClientHandler sender) {
        String[] parts = splitPasswordAndPublicKey(msg.getData());

        if (parts == null) {
            log.warn("로그인 실패 userId={}", msg.getSender());
            send(sender, MessageType.LOGIN_FAIL, "로그인 실패", null);
            return;
        }

        String password = parts[0];
        String publicKey = parts[1];

        boolean success = memberController.login(msg.getSender(), password);

        send(sender,
                success ? MessageType.LOGIN_SUCCESS : MessageType.LOGIN_FAIL,
                success ? "로그인 성공" : "로그인 실패",
                null);

        if (!success) {
            log.warn("로그인 실패. userId={}", msg.getSender());
            return;
        }

        // 로그인 성공 시 해당 핸들러에 사용자 ID 매핑
        sender.setUserId(msg.getSender());

        // 나중에 채팅할 때 암호화 하려면 상대방 공개키가 필요하니까 미리 저장
        publicKeyService.savePublicKey(msg.getSender(), publicKey);

        log.info("로그인 성공. userId={}", msg.getSender());
    }

    public void logout(Message msg, ClientHandler sender) {
        // 저장된 키 지우고 소켓 연결 종료
        publicKeyService.remove(msg.getSender());
        sender.close();

        log.info("로그아웃 처리. userId={}", msg.getSender());
    }

    /**
     * "비밀번호::공개키" 형식의 문자열을 쪼개는 헬퍼 메서드
     */
    private String[] splitPasswordAndPublicKey(String data) {
        if (data == null || !data.contains("::")) {
            return null;
        }

        // 구분자(::) 기준으로 딱 두 조각만 나눔
        String[] parts = data.split("::", 2);

        if (parts.length != 2) {
            return null;
        }

        // 둘 중 하나라도 비어있으면 안 됨
        if (parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }

        return parts;
    }

    /**
     * 클라이언트 응답 메시지
     */
    private void send(ClientHandler client, MessageType type, String data, String roomId) {
        Message response = Message.builder()
                .type(type)
                .sender("SERVER")
                .roomId(roomId)
                .data(data)
                .build();

        client.send(JsonUtil.toJson(response));
    }
}