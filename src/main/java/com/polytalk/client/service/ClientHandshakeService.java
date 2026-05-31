package com.polytalk.client.service;

import com.polytalk.client.network.MessageSender;
import com.polytalk.client.network.ServerResponseReader;
import com.polytalk.client.state.ClientState;
import com.polytalk.crypto.*;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;

import javax.crypto.SecretKey;
import java.security.PublicKey;

/**
 * 클라이언트 측 보안 핸드셰이크 처리.
 */
public class ClientHandshakeService {

    private final MessageSender sender;
    private final ServerResponseReader reader;

    public ClientHandshakeService(MessageSender sender, ServerResponseReader reader) {
        this.sender = sender;
        this.reader = reader;
    }

    public void handshake(ClientState state) throws Exception {

        // 내 공개키와 지원 알고리즘 목록을 묶어 서버로 전송한다
        String pub = KeyUtil.publicKeyToBase64(state.getKeyPair().getPublic());
        String suites = String.join(",", CipherSuite.supportedNames());

        sender.send(Message.builder()
                .type(MessageType.CIPHER_SUITE_REQUEST)
                .data(suites + "::" + pub)
                .build());

        // 서버 응답 대기 (1-RTT: 알고리즘·공개키·SEED를 한 번에 수신)
        Message res = reader.readUntil(state, MessageType.CIPHER_SUITE_RESPONSE);

        // 프로토콜 규격: [알고리즘 :: 서버 공개키 :: 암호화된 SEED]
        String[] parts = res.getData().split("::");

        if (parts.length != 3) {
            throw new IllegalStateException("서버 응답 형식이 올바르지 않습니다. 구분자(::)를 확인하세요.");
        }

        String selectedSuite = parts[0];
        PublicKey serverKey = KeyUtil.base64ToPublicKey(parts[1]);
        String encryptedSeed = parts[2];

        // 내 개인키(C_PRI)와 서버 공개키(S_PUB)로 임시 공유키(SHARED_SECRET)를 계산한다
        SecretKey shared = EcdhUtil.generateSecretKey(
                state.getKeyPair().getPrivate(),
                serverKey
        );

        // 서버가 보낸 E(SEED)를 방금 만든 공유키로 복호화해서 SEED 획득
        byte[] seed = AesGcmUtil.decryptToBytes(encryptedSeed, shared);

        // 획득한 SEED를 PBKDF2로 1000회 반복 유도해 최종 AES 키를 만든다
        SecretKey finalKey = KeyDerivationUtil.deriveAesKey(seed, 1000);

        // 보안 세션 준비 완료. 상태값에 저장해둠.
        state.setSelectedCipherSuite(selectedSuite);
        state.setServerSessionKey(finalKey);
        state.setHandshakeCompleted(true);
    }
}