package com.polytalk.server.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytalk.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 회원 정보를 JSON 파일에 저장하고 조회하는 클래스
 */
@Slf4j
public class MemberRepository {

    // 데이터 저장 파일 경로
    private static final String FILE_PATH = "data/members.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 회원 정보 저장
     * 멀티스레드라 싱크로나이즈드 사용
     */
    public synchronized void save(Member member) {
        try {
            File file = new File(FILE_PATH);
            // 1. 디렉토리 없음 자동 생성
            file.getParentFile().mkdirs();

            List<Member> members = findAll();

            // 2. 기존 정보가 있다면 지우고 새로 추가
            members.removeIf(m -> m.getId().equals(member.getId()));
            members.add(member);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, members);
            log.info("회원정보 저장 완료: {}", member.getId());

        } catch (Exception e) {
            log.error("회원정보 저장 실패", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 전체 회원 목록 불러오기
     */
    public synchronized List<Member> findAll() {
        try {
            File file = new File(FILE_PATH);

            // 파일이 아직 없으면 빈 리스트 반환 (첫 가입자 처리)
            if (!file.exists()) {
                return new ArrayList<>();
            }

            return mapper.readValue(file, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("회원정보 읽기 실패", e);
            return new ArrayList<>();
        }
    }

    // 아이디로 회원 찾기
    public synchronized Optional<Member> findById(String id) {
        return findAll().stream().filter(m -> m.getId().equals(id)).findFirst();
    }
}