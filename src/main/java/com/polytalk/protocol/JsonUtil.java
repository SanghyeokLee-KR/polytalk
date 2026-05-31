package com.polytalk.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 객체와 JSON 문자열을 변환하는 클래스
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    // 자바 객체 -> JSON
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    // 제네릭 타입(clazz)으로 Message, 도메인 객체 등 어떤 클래스든 역직렬화
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            // clazz 매개변수를 통해 런타임에 어떤 객체로 만들지 결정
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}