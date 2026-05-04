package com.polytalk.client.service;

import com.polytalk.client.network.MessageSender;
import com.polytalk.client.network.ServerResponseReader;
import com.polytalk.client.state.ClientState;
import com.polytalk.crypto.EcdhUtil;
import com.polytalk.crypto.KeyStoreUtil;
import com.polytalk.crypto.KeyUtil;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;

@Slf4j
public class ClientAuthService {

    private final MessageSender sender;
    private final ServerResponseReader reader;
    private final ClientHandshakeService handshakeService;

    public ClientAuthService(MessageSender sender, ServerResponseReader reader) {
        this.sender = sender;
        this.reader = reader;
        this.handshakeService = new ClientHandshakeService(sender, reader);
    }

    public boolean signUp(String userId, String password) throws Exception {
        KeyPair keyPair = EcdhUtil.generateKeyPair();
        KeyStoreUtil.saveKeyPair(userId, keyPair);

        String publicKey = KeyUtil.publicKeyToBase64(keyPair.getPublic());

        Message request = Message.builder().type(MessageType.SIGN_UP).sender(userId).data(password + "::" + publicKey).build();

        sender.send(request);

        ClientState tempState = new ClientState(userId, keyPair);
        Message response = reader.readUntil(tempState, MessageType.SIGN_UP_SUCCESS, MessageType.SIGN_UP_FAIL);

        boolean success = response.getType() == MessageType.SIGN_UP_SUCCESS;

        log.info("회원가입 응답 수신. userId={}, success={}", userId, success);

        return success;
    }

    public ClientState login(String userId, String password) throws Exception {
        KeyPair keyPair = KeyStoreUtil.loadKeyPair(userId);

        if (keyPair == null) {
            log.warn("로그인 실패 - 클라이언트 키쌍 없음. userId={}", userId);
            return null;
        }

        ClientState state = new ClientState(userId, keyPair);
        String publicKey = KeyUtil.publicKeyToBase64(keyPair.getPublic());

        Message request = Message.builder().type(MessageType.LOGIN).sender(userId).data(password + "::" + publicKey).build();

        sender.send(request);

        Message response = reader.readUntil(state, MessageType.LOGIN_SUCCESS, MessageType.LOGIN_FAIL);

        if (response.getType() == MessageType.LOGIN_FAIL) {
            log.warn("로그인 실패. userId={}", userId);
            return null;
        }

        log.info("로그인 성공. userId={}", userId);

        // 로그인 성공 직후 서버-클라이언트 핸드셰이크를 수행한다.
        // 이 단계에서 Cipher Suite 협상, ECDH, SEED 교환, SecretKey 유도가 진행된다.
        handshakeService.handshake(state);

        return state;
    }
}