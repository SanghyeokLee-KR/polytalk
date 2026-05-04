package com.polytalk.server.service;

import com.polytalk.domain.ChatLog;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.server.ClientHandler;
import com.polytalk.server.ClientManager;
import com.polytalk.server.repository.ChatLogRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 서버에서 채팅 메시지를 저장하고 전달하는 서비스
 */
@Slf4j
public class ServerChatService {

    private final ChatLogRepository chatLogRepository;
    private final ClientManager clientManager;

    public ServerChatService(
            ChatLogRepository chatLogRepository,
            ClientManager clientManager
    ) {
        this.chatLogRepository = chatLogRepository;
        this.clientManager = clientManager;
    }

    public void handleChat(Message msg, ClientHandler sender) {
        // *중요* -> 서버는 복호화 안하고 암호문 그대로 저장
        ChatLog chatLog = ChatLog.builder()
                .roomId(msg.getRoomId())
                .sender(msg.getSender())
                .encryptedData(msg.getData())
                .sentAt(msg.getSentAt())
                .build();

        chatLogRepository.save(chatLog);

        log.info("채팅 수신. roomId={}, sender={}",
                msg.getRoomId(),
                msg.getSender());

        // 같은 방에 있는 다른 사용자에게 전달
        clientManager.broadcastRoom(JsonUtil.toJson(msg), msg.getRoomId(), sender);
    }
}