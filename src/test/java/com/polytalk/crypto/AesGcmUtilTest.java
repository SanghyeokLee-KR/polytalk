package com.polytalk.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;

class AesGcmUtilTest {

    @Test
    void encryptAndDecrypt() throws Exception {
        // given
        SecretKey key = AesGcmUtil.generateKey();
        String plainText = "hello";

        // when
        String encrypted = AesGcmUtil.encrypt(plainText, key);
        String decrypted = AesGcmUtil.decrypt(encrypted, key);

        // then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void encryptResultHasIvAndCipherText() throws Exception {
        // given
        SecretKey key = AesGcmUtil.generateKey();
        String plainText = "test-message";

        // when
        String encrypted = AesGcmUtil.encrypt(plainText, key);

        // then
        String[] parts = encrypted.split(":");

        assertThat(parts).hasSize(2);
        assertThat(parts[0]).isNotBlank();
        assertThat(parts[1]).isNotBlank();
    }

    @Test
    void samePlainTextShouldMakeDifferentEncryptedText() throws Exception {
        // given
        SecretKey key = AesGcmUtil.generateKey();
        String plainText = "same-message";

        // when
        String encrypted1 = AesGcmUtil.encrypt(plainText, key);
        String encrypted2 = AesGcmUtil.encrypt(plainText, key);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }
}