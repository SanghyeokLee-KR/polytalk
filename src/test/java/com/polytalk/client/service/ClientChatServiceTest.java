package com.polytalk.client.service;

import com.polytalk.client.network.MessageSender;
import com.polytalk.client.service.ClientChatService;
import com.polytalk.client.state.ClientState;
import com.polytalk.crypto.AesGcmUtil;
import com.polytalk.crypto.EcdhUtil;
import com.polytalk.protocol.Message;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;

class ClientChatServiceTest {

    @Test
    void 보안키가_있으면_채팅을_암호화해서_보낸다() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);

        MessageSender messageSender = new MessageSender(printWriter);
        ClientChatService chatService = new ClientChatService(messageSender);

        KeyPair keyPair = EcdhUtil.generateKeyPair();
        SecretKey aesKey = AesGcmUtil.generateKey();

        ClientState state = new ClientState("userA", keyPair);
        state.enterRoom("room1", "테스트방");
        state.setAesKey(aesKey);
        state.setKeyVerified(true);

        String plainText = "안녕하세요";

        Message message = chatService.sendChat(state, plainText);

        assertThat(message.getData()).isNotEqualTo(plainText);
        assertThat(message.getRoomId()).isEqualTo("room1");
        assertThat(message.getSender()).isEqualTo("userA");
    }

    @Test
    void 보안키가_없으면_채팅을_보낼_수_없다() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);

        MessageSender messageSender = new MessageSender(printWriter);
        ClientChatService chatService = new ClientChatService(messageSender);

        KeyPair keyPair = EcdhUtil.generateKeyPair();
        ClientState state = new ClientState("userA", keyPair);
        state.enterRoom("room1", "테스트방");

        assertThatThrownBy(() -> chatService.sendChat(state, "안뇽"))
                .isInstanceOf(IllegalStateException.class);
    }
}