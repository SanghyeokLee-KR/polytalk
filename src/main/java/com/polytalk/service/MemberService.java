package com.polytalk.service;

import com.polytalk.crypto.PasswordUtil;
import com.polytalk.domain.Member;
import com.polytalk.server.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

/**
 * 회원가입과 로그인 처리를 담당하는 서비스
 */
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입
    public boolean signUp(Member member) {
        if (member == null) return false;
        if (isBlank(member.getId())) return false;
        if (isBlank(member.getPassword())) return false;
        if (isBlank(member.getPublicKey())) return false; // 공개키 누락 확인

        // 이미 존재하는 아이디는 가입 불가
        if (memberRepository.findById(member.getId()).isPresent()) {
            return false;
        }

        // 비밀번호는 원문 대신 해시값으로 저장
        member.setPassword(PasswordUtil.hash(member.getPassword()));

        memberRepository.save(member);
        System.out.println("[회원가입 성공] 새 회원이 등록되었습니다: " + member.getId());
        return true;
    }

    // 로그인
    public boolean login(String id, String password) {
        if (isBlank(id)) return false;
        if (isBlank(password)) return false;

        return memberRepository.findById(id).filter(m -> PasswordUtil.matches(password, m.getPassword())).isPresent();
    }

    // 입력값 비어있는지
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}