package com.polytalk.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytalk.client.network.MessageSender;
import com.polytalk.client.state.ClientState;
import com.polytalk.crypto.AesGcmUtil;
import com.polytalk.domain.ChatLog;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 채팅 메시지를 서버로 보내는 서비스
 */
public class ClientChatService {

    private final MessageSender sender;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClientChatService(MessageSender sender) {
        this.sender = sender;
    }

    public Message sendChat(ClientState state, String plainText) throws Exception {
        if (!state.isKeyReady()) {
            throw new IllegalStateException("상대방 공개키 검증 후 채팅 가능합니다.");
        }

        // 1단계: 평문 메시지를 AES/GCM으로 암호화한다.
        String encrypted = AesGcmUtil.encrypt(plainText, state.getAesKey());

        String sentAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Message chat = Message.builder().type(MessageType.CHAT).roomId(state.getCurrentRoomId()).sender(state.getUserId()).data(encrypted).sentAt(sentAt).build();

        // 2단계: 서버에는 평문이 아니라 암호문만 보낸다.
        sender.send(chat);
        return chat;
    }

    // 복호화
    public String decryptChat(ClientState state, Message msg) throws Exception {
        if (!state.isKeyReady()) {
            throw new IllegalStateException("복호화 키가 없습니다.");
        }

        return AesGcmUtil.decrypt(msg.getData(), state.getAesKey());
    }

    public List<ChatLog> parseHistory(String data) throws Exception {
        return mapper.readValue(data, new TypeReference<>() {
        });
    }

    // 기록
    public String decryptHistory(ClientState state, ChatLog log) throws Exception {
        return AesGcmUtil.decrypt(log.getEncryptedData(), state.getAesKey());
    }
}
