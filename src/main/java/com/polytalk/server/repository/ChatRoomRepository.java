package com.polytalk.server.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytalk.domain.ChatRoom;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 채팅방 정보를 JSON 파일에 저장하고 조회하는 클래스
 */
@Slf4j
public class ChatRoomRepository {

    // 경로
    private static final String FILE_PATH = "data/chat_rooms.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 방 정보 저장 (동시에 여러 명이 방 만들 때 꼬이지 않게 싱크로 나이즈드 사용)
     */
    public synchronized void save(ChatRoom room) {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            List<ChatRoom> rooms = findAll();

            // 덮어쓰기
            rooms.removeIf(r -> r.getRoomId().equals(room.getRoomId()));
            rooms.add(room);

            // 정렬
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rooms);

        } catch (Exception e) {
            log.error("채팅방 저장 실패", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 파일에서 모든 채팅방 목록을 읽어옴
     */
    public synchronized List<ChatRoom> findAll() {
        try {
            File file = new File(FILE_PATH);

            // 파일 없음 빈 리스트
            if (!file.exists()) {
                return new ArrayList<>();
            }

            // JSON 리스트를 List<ChatRoom> 객체로 변환
            return mapper.readValue(file, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("채팅방 읽기 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * 방 번호(ID)로 특정 방 정보 찾기
     */
    public synchronized Optional<ChatRoom> findById(String roomId) {
        return findAll().stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst();
    }

    /**
     * 채팅 방 폭파
     */
    public synchronized void deleteByRoomId(String roomId) {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            List<ChatRoom> rooms = findAll();
            rooms.removeIf(room -> room.getRoomId().equals(roomId));

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rooms);

        } catch (Exception e) {
            log.error("채팅방 삭제 실패", e);
            throw new RuntimeException(e);
        }
    }
}