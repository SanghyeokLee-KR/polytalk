package com.polytalk.domain;

import com.polytalk.domain.ChatRoom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChatRoomTest {

    @Test
    void 멤버를_추가할_수_있다() {
        ChatRoom room = ChatRoom.builder()
                .roomId("room1")
                .roomName("테스트방")
                .build();

        room.addMember("userA");

        assertThat(room.getMemberCount()).isEqualTo(1);
        assertThat(room.getMembers()).contains("userA");
    }

    @Test
    void 같은_멤버는_중복_추가되지_않는다() {
        ChatRoom room = ChatRoom.builder()
                .roomId("room1")
                .roomName("테스트방")
                .build();

        room.addMember("userA");
        room.addMember("userA");

        assertThat(room.getMemberCount()).isEqualTo(1);
    }

    @Test
    void 두명이면_방이_가득찬다() {
        ChatRoom room = ChatRoom.builder()
                .roomId("room1")
                .roomName("테스트방")
                .build();

        room.addMember("userA");
        room.addMember("userB");

        assertThat(room.isFull()).isTrue();
    }
}