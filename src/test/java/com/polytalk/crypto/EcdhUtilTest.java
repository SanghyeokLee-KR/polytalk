package com.polytalk.crypto;

import com.polytalk.crypto.EcdhUtil;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;

class EcdhUtilTest {

    @Test
    void 서로_같은_AES키를_만든다() throws Exception {
        KeyPair a = EcdhUtil.generateKeyPair();
        KeyPair b = EcdhUtil.generateKeyPair();

        SecretKey keyA = EcdhUtil.generateSecretKey(a.getPrivate(), b.getPublic());
        SecretKey keyB = EcdhUtil.generateSecretKey(b.getPrivate(), a.getPublic());

        assertThat(keyA.getEncoded()).isEqualTo(keyB.getEncoded());
    }
}