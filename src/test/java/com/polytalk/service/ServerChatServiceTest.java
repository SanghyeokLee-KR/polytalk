package com.polytalk.service;

import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;
import com.polytalk.server.ClientManager;
import com.polytalk.server.repository.ChatLogRepository;
import com.polytalk.server.service.ServerChatService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ServerChatServiceTest {

    @Test
    void 서버는_채팅_평문이_아닌_암호문을_저장한다() {
        ChatLogRepository chatLogRepository = new ChatLogRepository();
        ClientManager clientManager = new ClientManager();

        ServerChatService chatService =
                new ServerChatService(chatLogRepository, clientManager);

        Message message = Message.builder()
                .type(MessageType.CHAT)
                .roomId("room-test")
                .sender("userA")
                .data("encrypted-data")
                .sentAt("2026-05-03 12:00:00")
                .build();

        chatService.handleChat(message, null);

        boolean exists = chatLogRepository.findByRoomId("room-test")
                .stream()
                .anyMatch(log ->
                        log.getEncryptedData().equals("encrypted-data")
                                && log.getSender().equals("userA")
                );

        assertThat(exists).isTrue();

        chatLogRepository.deleteByRoomId("room-test");
    }
}