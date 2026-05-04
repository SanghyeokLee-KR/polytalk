package com.polytalk.domain;

import lombok.*;

/**
 * 채팅 기록을 저장할 때 사용할 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatLog {

    private String roomId;
    private String sender;

    // 서버에는 평문이 아니라 암호문만 저장
    private String encryptedData;

    private String sentAt; // 시간
}