package com.polytalk.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoom {

    private String roomId;
    private String roomName;

    // 방 비밀번호는 평문이 아니라 해시값으로 저장
    private String password;

    @Builder.Default // 빌더 기본값
    private List<String> members = new ArrayList<>();

    private String createdAt;

    @JsonIgnore
    public boolean isFull() {
        return members != null && members.size() >= 2;
    }

    @JsonIgnore
    public int getMemberCount() {
        return members == null ? 0 : members.size();
    }

    public void addMember(String userId) {
        if (members == null) {
            members = new ArrayList<>();
        }

        if (!members.contains(userId)) {
            members.add(userId);
        }
    }

    public void removeMember(String userId) {
        if (members != null) {
            members.remove(userId);
        }
    }
}