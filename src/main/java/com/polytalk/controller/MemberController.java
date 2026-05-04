package com.polytalk.controller;

import com.polytalk.domain.Member;
import com.polytalk.service.MemberService;
import lombok.RequiredArgsConstructor;

/**
 * 회원 요청 값만 넘겨주는 컨트롤러 -> 서비스로
 */
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    public boolean signUp(String id, String password, String publicKey) {
        Member member = Member.builder()
                .id(id)
                .password(password)
                .publicKey(publicKey)
                .build();

        return memberService.signUp(member);
    }

    // 로그인
    public boolean login(String id, String password) {
        return memberService.login(id, password);
    }
}