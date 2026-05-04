package com.polytalk.server.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytalk.domain.ChatLog;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 모든 채팅 메시지 기록을 JSON 파일로 관리하는 저장소
 */
@Slf4j
public class ChatLogRepository {

    // 경로
    private static final String FILE_PATH = "data/chat_logs.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 새로운 채팅 메시지 저장
     */
    public synchronized void save(ChatLog chatLog) {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            // 기존 로그 싹 다 가져와서 새 메시지 하나 추가하고 다시 저장
            List<ChatLog> logs = findAll();
            logs.add(chatLog);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, logs);

        } catch (Exception e) {
            log.error("대화내역 저장 실패", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 파일에 적힌 모든 채팅 기록 읽어오기
     */
    public synchronized List<ChatLog> findAll() {
        try {
            File file = new File(FILE_PATH);

            if (!file.exists()) {
                return new ArrayList<>();
            }

            // JSON 배열을 ChatLog 리스트 객체로 변환
            return mapper.readValue(file, new TypeReference<List<ChatLog>>() {});

        } catch (Exception e) {
            log.error("대화내역 읽기 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * 특정 방(Room ID)로 대화 내역만 필터링해서 가져오기
     * 그 전에 메세지 내역 출력
     */
    public synchronized List<ChatLog> findByRoomId(String roomId) {
        return findAll().stream()
                .filter(log -> log.getRoomId().equals(roomId))
                .toList();
    }

    /**
     * 방 폭파 전체 다 삭제
     */
    public synchronized void deleteByRoomId(String roomId) {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            List<ChatLog> logs = findAll();

            // 리스트에서 해당 방 번호를 가진 로그만 싹 제거
            logs.removeIf(log -> log.getRoomId().equals(roomId));

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, logs);
            log.info("대화내역 삭제 완료. roomId={}", roomId);

        } catch (Exception e) {
            log.error("대화내역 삭제 실패", e);
            throw new RuntimeException(e);
        }
    }
}