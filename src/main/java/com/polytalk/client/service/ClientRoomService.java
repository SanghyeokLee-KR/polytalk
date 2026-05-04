package com.polytalk.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytalk.client.network.MessageSender;
import com.polytalk.client.network.ServerResponseReader;
import com.polytalk.client.state.ClientState;
import com.polytalk.domain.ChatRoom;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;

import java.util.List;

/**
 * 클라이언트 룸 서비스
 */
public class ClientRoomService {

    private final MessageSender sender;
    private final ServerResponseReader reader;
    private final ObjectMapper mapper = new ObjectMapper(); // JSON 변환기

    public ClientRoomService(MessageSender sender, ServerResponseReader reader) {
        this.sender = sender;
        this.reader = reader;
    }

    // 서버에 방 목록을 요청하고 JSON을 리스트로 변환해서 반환
    public List<ChatRoom> findRooms(ClientState state) throws Exception {
        sender.send(Message.builder().type(MessageType.ROOM_LIST).sender(state.getUserId()).build());

        Message response = reader.readUntil(state, MessageType.ROOM_LIST_RESULT);
        return mapper.readValue(response.getData(), new TypeReference<List<ChatRoom>>() {
        });
    }

    // 방 생성
    public ChatRoom createRoom(ClientState state, String roomName, String roomPassword) throws Exception {
        sender.send(Message.builder().type(MessageType.ROOM_CREATE).sender(state.getUserId()).data(roomName + "::" + roomPassword).build());

        Message response = reader.readUntil(state, MessageType.ROOM_CREATE_SUCCESS, MessageType.ROOM_CREATE_FAIL);

        if (response.getType() == MessageType.ROOM_CREATE_FAIL) return null;

        ChatRoom room = mapper.readValue(response.getData(), ChatRoom.class);
        state.enterRoom(room.getRoomId(), room.getRoomName()); // 내 상태 업데이트
        return room;
    }

    // 방 입장
    public ChatRoom joinRoom(ClientState state, String roomId, String roomPassword) throws Exception {
        sender.send(Message.builder().type(MessageType.ROOM_JOIN).sender(state.getUserId()).roomId(roomId).data(roomPassword).build());

        Message response = reader.readUntil(state, MessageType.ROOM_JOIN_SUCCESS, MessageType.ROOM_JOIN_FAIL);

        if (response.getType() == MessageType.ROOM_JOIN_FAIL) return null;

        ChatRoom room = mapper.readValue(response.getData(), ChatRoom.class);
        state.enterRoom(room.getRoomId(), room.getRoomName());
        return room;
    }

    // 방 입장 직후 이전 대화 내역 읽기
    public Message readHistory(ClientState state) throws Exception {
        // 내부적으로 보안 키 교환(PUBLIC_KEY)이 먼저 오면 그것부터 처리하고 내역을 반환함
        return reader.readUntil(state, MessageType.CHAT_HISTORY);
    }

    // 방 나가기
    public void leaveRoom(ClientState state) {
        sender.send(Message.builder().type(MessageType.ROOM_LEAVE).roomId(state.getCurrentRoomId()).sender(state.getUserId()).build());

        state.clearRoom();
    }

    // 로그아웃
    public void logout(ClientState state) {
        sender.send(Message.builder().type(MessageType.LOGOUT).sender(state.getUserId()).build());
        state.setRunning(false);
    }

    // 방 폭파
    public void destroyRoom(ClientState state) {
        sender.send(Message.builder().type(MessageType.ROOM_DESTROY).roomId(state.getCurrentRoomId()).sender(state.getUserId()).build());

        state.clearRoom();
    }
}