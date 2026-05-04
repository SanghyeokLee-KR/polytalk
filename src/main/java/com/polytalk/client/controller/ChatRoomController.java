package com.polytalk.client.controller;

import com.polytalk.client.network.MessageReceiver;
import com.polytalk.client.network.SocketClient;
import com.polytalk.client.service.ClientChatService;
import com.polytalk.client.service.ClientRoomService;
import com.polytalk.client.service.ClientSecurityService;
import com.polytalk.client.state.ClientState;
import com.polytalk.client.ui.ChatConsoleUI;
import com.polytalk.domain.ChatLog;
import com.polytalk.domain.ChatRoom;
import com.polytalk.protocol.Message;

import java.util.List;

/**
 * 실제 채팅방 내부의 동작
 * 메세지 전송, 스레드 관리, 명령어 처리
 */
public class ChatRoomController {

    private final SocketClient socketClient;
    private final ClientRoomService roomService;
    private final ClientChatService chatService;
    private final ClientSecurityService securityService;
    private final ChatConsoleUI chatUI;

    public ChatRoomController(SocketClient socketClient, ClientRoomService roomService, ClientChatService chatService, ClientSecurityService securityService, ChatConsoleUI chatUI) {
        this.socketClient = socketClient;
        this.roomService = roomService;
        this.chatService = chatService;
        this.securityService = securityService;
        this.chatUI = chatUI;
    }

    public void run(ClientState state, ChatRoom room) throws Exception {

        // 1. 방에 들어가자마자 이전 대화(History) 불러오기
        Message historyMessage = roomService.readHistory(state);

        chatUI.printChatHeader(room.getRoomName(), room.getMemberCount());

        // 2. 현재 보안 연결 상태와 이전 대화 내용 출력
        printSecurityStatus(state);
        printHistory(state, historyMessage);

        // 3. [중요] 실시간 메시지 수신을 위한 백그라운드 스레드 시작
        startReceiver(state);

        // 4. 명령어
        while (state.isRunning() && state.isInRoom()) {
            String input = chatUI.inputMessage(state.getUserId());

            if (input.equals("/나가기")) {
                roomService.leaveRoom(state);
                break;
            }

            if (input.equals("/방폭파")) {
                roomService.destroyRoom(state);
                break;
            }

            if (input.equals("/finger")) {
                printSecurityStatus(state);
                continue;
            }

            sendChat(state, input);
        }
    }

    // 1대1 암호화 키 만들었는지 확인하는 메서드
    private void printSecurityStatus(ClientState state) {
        if (state.hasTrustedPeer()) {
            chatUI.printInfo("[보안 연결 완료]");
            chatUI.printPublicKey(state.getTrustedPeerId(), state.getTrustedFingerprint());
            return;
        }

        chatUI.printInfo("[안내] 아직 상대방이 없어 보안 연결 대기 중입니다.");
    }

    // 전 대화 기록
    private void printHistory(ClientState state, Message historyMessage) throws Exception {
        chatUI.printHistoryTitle();

        List<ChatLog> logs = chatService.parseHistory(historyMessage.getData());

        if (logs.isEmpty()) {
            chatUI.printInfo("이전 대화가 없습니다.");
            return;
        }

        for (ChatLog log : logs) {
            try {
                // 내 키로 복호화 시도
                String plain = chatService.decryptHistory(state, log);
                chatUI.printHistoryLine(log.getSentAt(), log.getSender(), plain);
            } catch (Exception e) {
                // 키가 다름
                chatUI.printHistoryLine(log.getSentAt(), log.getSender(), "[보안키 검증 후 복호화 가능]");
            }
        }
    }
    /**
     * 메인 루프와 별개로,
     * 서버가 보내는 메시지를 계속 감시하는
     * '수신 전용 스레드'
     */
    private void startReceiver(ClientState state) {
        Thread receiver = new Thread(new MessageReceiver(socketClient.reader(), state, securityService, chatService, chatUI));

        receiver.setDaemon(true);
        receiver.start();
    }

    /**
     * 채팅 메시지를 암호화해서 서버로 전송
     */
    private void sendChat(ClientState state, String input) {
        try {
            Message sent = chatService.sendChat(state, input);
            chatUI.printMyChat(sent.getSentAt(), state.getUserId(), input);
        } catch (Exception e) {
            chatUI.printInfo("[차단] 상대방 입장 후 보안 연결 필요");
        }
    }
}