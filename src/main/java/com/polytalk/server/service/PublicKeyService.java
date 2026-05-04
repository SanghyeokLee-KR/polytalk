package com.polytalk.server.service;

import com.polytalk.domain.ChatRoom;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;
import com.polytalk.server.ClientHandler;
import com.polytalk.server.ClientManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 접속 중인 유저들의 공개키(Public Key) 보관 및 배포 서비스
 */
@Slf4j
public class PublicKeyService {

    private final ClientManager clientManager;

    // 접속자들의 공개키 저장소 (멀티스레드 환경이라 컨커런트 해시맵 사용)
    private final Map<String, String> publicKeys = new ConcurrentHashMap<>();

    public PublicKeyService(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    // 로그인 시 유저의 공개키를 메모리에 저장
    public void savePublicKey(String userId, String publicKey) {
        if (isBlank(userId)) return;
        if (isBlank(publicKey)) return;

        publicKeys.put(userId, publicKey);
        log.info("공개키 저장 완료. userId={}", userId);
    }

    /**
     * 방에 새로 들어온 사람에게 기존 멤버들의 공개키를 모두 전송
     * 수정 예정
     */
    public void sendRoomPublicKeys(ChatRoom room, ClientHandler receiver) {
        if (room == null || room.getMembers() == null) {
            return;
        }

        for (String memberId : room.getMembers()) {
            // 본인 키는 받을 필요 없으니 제외
            if (memberId.equals(receiver.getUserId())) {
                continue;
            }

            String publicKey = publicKeys.get(memberId);

            if (publicKey == null) {
                log.warn("공개키 전송 실패 - 공개키 없음. targetUserId={}, receiver={}",
                        memberId,
                        receiver.getUserId());
                continue;
            }

            // PUBLIC_KEY 타입 메시지에 담아서 전달
            Message message = Message.builder()
                    .type(MessageType.PUBLIC_KEY)
                    .roomId(room.getRoomId())
                    .sender(memberId)
                    .data(publicKey)
                    .build();

            receiver.send(JsonUtil.toJson(message));

            log.info("방 기존 멤버 공개키 전송. roomId={}, fromUser={}, toUser={}",
                    room.getRoomId(),
                    memberId,
                    receiver.getUserId());
        }
    }

    /**
     * 방에 있는 유저의 공개키를 브로드캐스트
     */
    public void broadcastPublicKeyToRoom(String userId, String roomId, ClientHandler sender) {
        String publicKey = publicKeys.get(userId);

        if (publicKey == null) {
            log.warn("방 공개키 브로드캐스트 실패 - 공개키 없음. userId={}, roomId={}",
                    userId,
                    roomId);
            return;
        }

        Message message = Message.builder()
                .type(MessageType.PUBLIC_KEY)
                .roomId(roomId)
                .sender(userId)
                .data(publicKey)
                .build();

        // 방 전체에 새 멤버의 공개키를 뿌림
        clientManager.broadcastRoom(JsonUtil.toJson(message), roomId, sender);

        log.info("방 새 멤버 공개키 브로드캐스트. roomId={}, userId={}",
                roomId,
                userId);
    }

    // 로그아웃 시 저장된 키 삭제
    public void remove(String userId) {
        if (userId == null) {
            return;
        }

        publicKeys.remove(userId);
        log.info("공개키 제거 완료. userId={}", userId);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}