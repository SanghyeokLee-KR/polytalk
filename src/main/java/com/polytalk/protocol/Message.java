package com.polytalk.protocol;

import lombok.*;

/**
 * 서버와 클라이언트가 주고받는 메시지 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    private MessageType type; // 메세지 타입
    private String roomId; // 방 번호
    private String sender; // 보낸 사람
    private String data; // 메시지
    private String sentAt; // 시간
}
