package com.polytalk.server.service;

import com.polytalk.domain.ChatRoom;
import com.polytalk.server.ClientHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 방 입장 시 공개키 교환을 처리하는 서비스
 */
@Slf4j
public class RoomKeyExchangeService {

    private final PublicKeyService publicKeyService;

    public RoomKeyExchangeService(PublicKeyService publicKeyService) {
        this.publicKeyService = publicKeyService;
    }

    public void exchangeKeysOnJoin(ChatRoom room, ClientHandler sender) {
        log.info("방 공개키 교환 시작. roomId={}, userId={}",
                room.getRoomId(),
                sender.getUserId());

        // 새로 들어온 사용자에게 기존 멤버의 공개키 전송
        publicKeyService.sendRoomPublicKeys(room, sender);

        // 기존 멤버에게 새 사용자의 공개키 전송
        publicKeyService.broadcastPublicKeyToRoom(
                sender.getUserId(),
                room.getRoomId(),
                sender
        );

        log.info("방 공개키 교환 완료. roomId={}, userId={}",
                room.getRoomId(),
                sender.getUserId());
    }
}