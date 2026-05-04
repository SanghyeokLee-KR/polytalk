package com.polytalk.client.state;

import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKey;
import java.security.KeyPair;

@Getter
@Setter
public class ClientState {

    private final String userId;
    private final KeyPair keyPair; // 개인키 + 공개키

    // 1. 보안 관련 (서버/상대방)
    private SecretKey aesKey; // 방에서 상대방과 채팅할 때 사용하는 AES 키
    private SecretKey serverSessionKey; // 서버와 핸드셰이크 후 만들어진 세션 키
    private String selectedCipherSuite; // 선택된 Cipher 알고리즘
    private boolean handshakeCompleted; // 서버와 핸드셰이크 완료 여부
    private boolean keyVerified; // trust 여부

    // 2. 실행 및 방 상태
    private boolean running = true;
    private boolean inRoom = false;
    private String currentRoomId;
    private String currentRoomName;

    // 3. 아직 검증 안 된 키
    private String pendingPeerId; // 검증 대기 중인 상대방 ID
    private String pendingPeerPublicKey; // 검증 대기 중인 상대방 공개키
    private String pendingFingerprint; // 검증 대기 중인 상대방 키 지문

    // 4. 검증 완료된 키
    private String trustedPeerId; // 검증 완료된 신뢰하는 상대방 ID
    private String trustedFingerprint; // 검증 완료된 신뢰하는 상대방 지문

    public ClientState(String userId, KeyPair keyPair) {
        this.userId = userId;
        this.keyPair = keyPair;
    }

    public boolean isKeyReady() {
        return aesKey != null && keyVerified;
    }

    public boolean hasPendingPublicKey() {
        return pendingPeerPublicKey != null;
    }

    public boolean hasTrustedPeer() {
        return trustedPeerId != null && trustedFingerprint != null;
    }

    public void enterRoom(String roomId, String roomName) {
        this.currentRoomId = roomId;
        this.currentRoomName = roomName;
        this.inRoom = true;
    }

    public void clearRoom() {
        this.inRoom = false;
        this.currentRoomId = null;
        this.currentRoomName = null;

        // 방을 나가면 방 채팅용 AES 키만 초기화한다.
        this.aesKey = null;
        this.keyVerified = false;

        clearPendingPublicKey();

        this.trustedPeerId = null;
        this.trustedFingerprint = null;
    }

    public void clearPendingPublicKey() {
        this.pendingPeerId = null;
        this.pendingPeerPublicKey = null;
        this.pendingFingerprint = null;
    }
}