package com.polytalk.crypto;

import java.security.SecureRandom;

public class SeedUtil {

    private static final int SEED_LENGTH = 32;

    private SeedUtil() {
    }

    // 최종 AES 키를 만들기 위한 랜덤 재료(SEED)를 만든다.
    public static byte[] generateSeed() {
        byte[] seed = new byte[SEED_LENGTH];
        new SecureRandom().nextBytes(seed);
        return seed;
    }
}