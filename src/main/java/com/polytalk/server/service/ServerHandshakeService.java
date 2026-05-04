package com.polytalk.server.service;

import com.polytalk.crypto.*;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;
import com.polytalk.server.ClientHandler;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 측 핸드셰이크 처리
 *
 * TODO: 다이어그램 다시 보고 로직 수정함
 */
@Slf4j
public class ServerHandshakeService implements HandshakeService {

    private static final int ITERATION = 1000; // PBKDF2 해싱 반복 횟수

    // 클라이언트별 세션 저장소 (멀티스레드니까 ConcurrentHashMap 필수)
    private final Map<ClientHandler, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public void negotiateCipherSuite(Message msg, ClientHandler sender) {
        try {
            // 클라이언트가 보낸 [알고리즘리스트 :: C_PUB] 쪼개기
            String[] parts = msg.getData().split("::", 2);

            if (parts.length != 2) {
                sendFail(sender, MessageType.CIPHER_SUITE_FAIL);
                return;
            }

            String supported = parts[0];
            String clientPub = parts[1];

            // 일단 첫 번째 알고리즘으로 픽스 (나중에 고도화할 때 로직 빼기)
            CipherSuite suite = select(supported);
            if (suite == null) {
                sendFail(sender, MessageType.CIPHER_SUITE_FAIL);
                return;
            }

            // 서버 측 ECDH 키쌍 생성
            KeyPair serverKey = EcdhUtil.generateKeyPair();
            PublicKey clientKey = KeyUtil.base64ToPublicKey(clientPub);

            // S_PRI + C_PUB 조합해서 임시 공유키(SHARED_SECRET) 연산
            SecretKey shared = EcdhUtil.generateSecretKey(
                    serverKey.getPrivate(), clientKey
            );

            // !! 다이어그램 요구사항 반영 !!
            // 서버가 주도적으로 랜덤 SEED를 뽑고, 임시 공유키로 암호화함 E(SEED)
            byte[] seed = SeedUtil.generateSeed();
            String encryptedSeed = AesGcmUtil.encrypt(seed, shared);

            // 서버는 방금 뽑은 SEED로 바로 최종 AES 키 유도해버림
            SecretKey finalKey = KeyDerivationUtil.deriveAesKey(seed, ITERATION);

            // 세션 객체에 최종 키 저장해두고 완료 처리
            Session session = new Session(shared, suite.getName());
            session.finalKey = finalKey;
            session.completed = true;
            sessions.put(sender, session);

            // 클라이언트한테 [알고리즘 :: S_PUB :: E(SEED)] 한 방에 내려줌
            String serverPubBase64 = KeyUtil.publicKeyToBase64(serverKey.getPublic());
            String responseData = suite.getName() + "::" + serverPubBase64 + "::" + encryptedSeed;

            Message res = Message.builder()
                    .type(MessageType.CIPHER_SUITE_RESPONSE)
                    .data(responseData)
                    .build();

            sender.send(JsonUtil.toJson(res));

            log.info("[Handshake] 보안 세션 수립 완료 및 SEED 전송. userId={}", sender.getUserId());

        } catch (Exception e) {
            log.error("핸드셰이크 터짐", e);
            sendFail(sender, MessageType.CIPHER_SUITE_FAIL);
        }
    }

    @Override
    public void receiveEncryptedSeed(Message msg, ClientHandler sender) {
        // 서버가 SEED를 직접 내려주도록 구조를 바꿨기 때문에 이 메서드는 이제 탈 일 없음.
        // 인터페이스 땜에 냅두고 혹시 몰라서 로그만 찍어둠
        log.warn("잘못된 접근: 현재 프로토콜에서는 클라이언트가 SEED를 보내지 않음.");
    }

    @Override
    public boolean isHandshakeCompleted(ClientHandler sender) {
        Session s = sessions.get(sender);
        return s != null && s.completed;
    }

    @Override
    public void remove(ClientHandler sender) {
        sessions.remove(sender);
    }

    private CipherSuite select(String supported) {
        return CipherSuite.fromName(supported.split(",")[0]).orElse(null);
    }

    private void sendFail(ClientHandler sender, MessageType type) {
        sender.send(JsonUtil.toJson(
                Message.builder().type(type).build()
        ));
    }

    // 핸들러별 암호화 진행 상태 추적용 내부 클래스
    private static class Session {
        private final SecretKey sharedKey;
        private final String suite;
        private SecretKey finalKey;
        private boolean completed;

        private Session(SecretKey sharedKey, String suite) {
            this.sharedKey = sharedKey;
            this.suite = suite;
        }
    }
}