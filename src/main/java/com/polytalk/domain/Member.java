package com.polytalk.domain;

import lombok.*;

/**
 * 회원 정보를 담는 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    private String id;
    private String password;
    private String publicKey;
}