package com.polytalk.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytalk.domain.ChatLog;
import com.polytalk.domain.ChatRoom;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;
import com.polytalk.protocol.MessageType;
import com.polytalk.server.ClientHandler;
import com.polytalk.server.ClientManager;
import com.polytalk.server.repository.ChatLogRepository;
import com.polytalk.server.repository.ChatRoomRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 채팅방 생성, 입장, 나가기, 폭파를 처리하는 서비스
 */
@Slf4j
public class ServerRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatLogRepository chatLogRepository;
    private final ClientManager clientManager;
    private final RoomPasswordService roomPasswordService;
    private final RoomKeyExchangeService roomKeyExchangeService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ServerRoomService(
            ChatRoomRepository chatRoomRepository,
            ChatLogRepository chatLogRepository,
            ClientManager clientManager,
            RoomPasswordService roomPasswordService,
            RoomKeyExchangeService roomKeyExchangeService
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatLogRepository = chatLogRepository;
        this.clientManager = clientManager;
        this.roomPasswordService = roomPasswordService;
        this.roomKeyExchangeService = roomKeyExchangeService;
    }

    public void sendRoomList(ClientHandler sender) throws Exception {
        List<ChatRoom> rooms = chatRoomRepository.findAll();

        send(sender,
                MessageType.ROOM_LIST_RESULT,
                mapper.writeValueAsString(rooms),
                null);

        log.info("채팅방 목록 전송. userId={}, roomCount={}",
                sender.getUserId(),
                rooms.size());
    }

    public void createRoom(Message msg, ClientHandler sender) {
        try {
            String[] parts = splitRoomNameAndPassword(msg.getData());

            if (parts == null) {
                send(sender, MessageType.ROOM_CREATE_FAIL, "방 생성 실패", null);
                return;
            }

            String roomName = parts[0];
            String rawPassword = parts[1];
            String roomId = UUID.randomUUID().toString();

            ChatRoom room = ChatRoom.builder()
                    .roomId(roomId)
                    .roomName(roomName)
                    .password(roomPasswordService.hash(rawPassword))
                    .members(new ArrayList<>())
                    .createdAt(now())
                    .build();

            room.addMember(msg.getSender());

            chatRoomRepository.save(room);
            sender.setCurrentRoomId(room.getRoomId());

            send(sender,
                    MessageType.ROOM_CREATE_SUCCESS,
                    mapper.writeValueAsString(room),
                    roomId);

            sendHistory(sender, room.getRoomId());

            log.info("방 생성 완료. roomId={}, roomName={}, owner={}",
                    roomId,
                    roomName,
                    msg.getSender());

        } catch (Exception e) {
            log.error("방 생성 처리 중 오류", e);
            send(sender, MessageType.ROOM_CREATE_FAIL, "방 생성 실패", null);
        }
    }

    public void joinRoom(Message msg, ClientHandler sender) {
        try {
            Optional<ChatRoom> optionalRoom = chatRoomRepository.findById(msg.getRoomId());

            if (optionalRoom.isEmpty()) {
                send(sender, MessageType.ROOM_JOIN_FAIL, "방 없음", msg.getRoomId());
                return;
            }

            ChatRoom room = optionalRoom.get();

            if (!roomPasswordService.matches(msg.getData(), room.getPassword())) {
                send(sender, MessageType.ROOM_JOIN_FAIL, "비밀번호 틀림", msg.getRoomId());
                return;
            }

            if (!room.getMembers().contains(msg.getSender())) {
                if (room.isFull()) {
                    send(sender, MessageType.ROOM_JOIN_FAIL, "방 가득참", msg.getRoomId());
                    return;
                }

                room.addMember(msg.getSender());
                chatRoomRepository.save(room);
            }

            sender.setCurrentRoomId(room.getRoomId());

            send(sender,
                    MessageType.ROOM_JOIN_SUCCESS,
                    mapper.writeValueAsString(room),
                    room.getRoomId());

            // 방 입장 후 상대방 공개키 교환
            roomKeyExchangeService.exchangeKeysOnJoin(room, sender);

            // 이전 채팅 기록 전송
            sendHistory(sender, room.getRoomId());

            log.info("방 입장 성공. roomId={}, userId={}, memberCount={}",
                    room.getRoomId(),
                    msg.getSender(),
                    room.getMemberCount());

        } catch (Exception e) {
            log.error("방 입장 처리 중 오류", e);
            send(sender, MessageType.ROOM_JOIN_FAIL, "방 입장 실패", msg.getRoomId());
        }
    }

    public void leaveRoom(Message msg, ClientHandler sender) {
        try {
            Optional<ChatRoom> optionalRoom = chatRoomRepository.findById(msg.getRoomId());

            if (optionalRoom.isPresent()) {
                ChatRoom room = optionalRoom.get();

                room.removeMember(msg.getSender());
                chatRoomRepository.save(room);

                log.info("방 나가기 완료. roomId={}, userId={}, memberCount={}",
                        msg.getRoomId(),
                        msg.getSender(),
                        room.getMemberCount());
            }

            sender.setCurrentRoomId(null);

        } catch (Exception e) {
            log.error("방 나가기 처리 중 오류", e);
        }
    }

    public void destroyRoom(Message msg) {
        try {
            chatRoomRepository.deleteByRoomId(msg.getRoomId());
            chatLogRepository.deleteByRoomId(msg.getRoomId());

            Message destroyed = Message.builder()
                    .type(MessageType.ROOM_DESTROYED)
                    .roomId(msg.getRoomId())
                    .sender("SERVER")
                    .data("채팅방이 폭파되었습니다.")
                    .build();

            clientManager.broadcastRoom(
                    JsonUtil.toJson(destroyed),
                    msg.getRoomId(),
                    null
            );

            for (ClientHandler client : clientManager.getClients()) {
                if (msg.getRoomId().equals(client.getCurrentRoomId())) {
                    client.setCurrentRoomId(null);
                }
            }

            log.info("방 폭파 완료. roomId={}, userId={}",
                    msg.getRoomId(),
                    msg.getSender());

        } catch (Exception e) {
            log.error("방 폭파 처리 중 오류", e);
        }
    }

    private void sendHistory(ClientHandler sender, String roomId) throws Exception {
        List<ChatLog> logs = chatLogRepository.findByRoomId(roomId);

        send(sender,
                MessageType.CHAT_HISTORY,
                mapper.writeValueAsString(logs),
                roomId);

        log.info("채팅 히스토리 전송. roomId={}, userId={}, logCount={}",
                roomId,
                sender.getUserId(),
                logs.size());
    }

    private String[] splitRoomNameAndPassword(String data) {
        if (data == null || !data.contains("::")) {
            return null;
        }

        String[] parts = data.split("::", 2);

        if (parts.length != 2) {
            return null;
        }

        if (parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }

        return parts;
    }

    private void send(ClientHandler client, MessageType type, String data, String roomId) {
        Message response = Message.builder()
                .type(type)
                .sender("SERVER")
                .roomId(roomId)
                .data(data)
                .build();

        client.send(JsonUtil.toJson(response));
    }

    private String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}