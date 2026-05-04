package com.polytalk.client.state;

import com.polytalk.client.state.ClientState;
import com.polytalk.crypto.AesGcmUtil;
import com.polytalk.crypto.EcdhUtil;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;

class ClientStateTest {

    @Test
    void 방에_입장하면_방정보가_저장된다() throws Exception {
        KeyPair keyPair = EcdhUtil.generateKeyPair();
        ClientState state = new ClientState("userA", keyPair);

        state.enterRoom("room1", "테스트방");

        assertThat(state.isInRoom()).isTrue();
        assertThat(state.getCurrentRoomId()).isEqualTo("room1");
        assertThat(state.getCurrentRoomName()).isEqualTo("테스트방");
    }

    @Test
    void 방을_나가면_방정보와_보안정보가_초기화된다() throws Exception {
        KeyPair keyPair = EcdhUtil.generateKeyPair();
        SecretKey aesKey = AesGcmUtil.generateKey();

        ClientState state = new ClientState("userA", keyPair);
        state.enterRoom("room1", "테스트방");

        state.setAesKey(aesKey);
        state.setKeyVerified(true);
        state.setTrustedPeerId("userB");
        state.setTrustedFingerprint("AA:BB:CC");

        state.clearRoom();

        assertThat(state.isInRoom()).isFalse();
        assertThat(state.getCurrentRoomId()).isNull();
        assertThat(state.getCurrentRoomName()).isNull();
        assertThat(state.getAesKey()).isNull();
        assertThat(state.isKeyVerified()).isFalse();
        assertThat(state.getTrustedPeerId()).isNull();
        assertThat(state.getTrustedFingerprint()).isNull();
    }
}