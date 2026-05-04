package com.polytalk.client.service;

import com.polytalk.client.state.ClientState;
import com.polytalk.crypto.EcdhUtil;
import com.polytalk.crypto.FingerprintUtil;
import com.polytalk.crypto.KeyUtil;
import com.polytalk.protocol.Message;

import javax.crypto.SecretKey;
import java.security.PublicKey;

/**
 * 상대방의 공개키를 처리 하고
 * 일 대 일 암호화 통신을 위한 AES 키를 생성하는 서비스
 */
public class ClientSecurityService {

    public void receivePeerPublicKey(Message msg, ClientState state) {
        // 본인이 보낸 공개키 메시지는 처리안함
        if (msg.getSender().equals(state.getUserId())) {
            return;
        }

        try {
            // 1. 공개키 지문(Fingerprint) 생성
            String fingerprint = FingerprintUtil.fingerprintFromBase64(msg.getData());

            // 2. 검증을 위해 상대방 정보를 임시(Pending) 상태로 저장
            state.setPendingPeerId(msg.getSender());
            state.setPendingPeerPublicKey(msg.getData());
            state.setPendingFingerprint(fingerprint);

            // 3. 수신한 Base64 공개키를 객체로 변환
            PublicKey peerPublicKey = KeyUtil.base64ToPublicKey(msg.getData());

            // 4. 내 개인키와 상대방 공개키를 조합(ECDH)하여 공통의 AES 키 생성
            SecretKey aesKey = EcdhUtil.generateSecretKey(
                    state.getKeyPair().getPrivate(),
                    peerPublicKey
            );

            // 5. 생성된 키를 세션 키로 설정 및 검증 상태 업데이트
            state.setAesKey(aesKey);
            state.setKeyVerified(true);

            // 6. 신뢰할 수 있는 사용자 정보로 등록 후 임시 데이터 삭제
            state.setTrustedPeerId(msg.getSender());
            state.setTrustedFingerprint(fingerprint);
            state.clearPendingPublicKey();

        } catch (Exception e) {
            throw new RuntimeException("보안 연결 생성 중 오류 발생", e);
        }
    }
}