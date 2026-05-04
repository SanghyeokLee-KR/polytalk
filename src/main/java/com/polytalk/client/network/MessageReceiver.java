package com.polytalk.client.network;

import com.polytalk.client.service.ClientChatService;
import com.polytalk.client.service.ClientSecurityService;
import com.polytalk.client.state.ClientState;
import com.polytalk.client.ui.ChatConsoleUI;
import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;

import java.io.BufferedReader;
import java.net.SocketTimeoutException;

/**
 * 백그라운드에서 서버의 메시지를
 * 실시간으로 읽어 처리하는 리시버 파일 (스레드)
 */
public class MessageReceiver implements Runnable {

    private final BufferedReader reader;
    private final ClientState state;
    private final ClientSecurityService securityService;
    private final ClientChatService chatService;
    private final ChatConsoleUI chatUI;

    public MessageReceiver(BufferedReader reader, ClientState state, ClientSecurityService securityService, ClientChatService chatService, ChatConsoleUI chatUI) {
        this.reader = reader;
        this.state = state;
        this.securityService = securityService;
        this.chatService = chatService;
        this.chatUI = chatUI;
    }

    @Override
    public void run() {
        try {
            // 내가 방에 있고 프로그램이 돌아가는 동안 무한 루프
            while (state.isRunning() && state.isInRoom()) {
                String line;

                try {
                    line = reader.readLine(); // 서버의 메시지를 기다림
                } catch (SocketTimeoutException e) {
                    // 아까 0.5로 로직
                    continue;
                }

                if (line == null) return; // 서버 연결 끊기면 종료

                Message msg = JsonUtil.fromJson(line, Message.class);

                switch (msg.getType()) {
                    case PUBLIC_KEY: // 새로운 사람이 들어와서 키 교환이 필요할 때
                        securityService.receivePeerPublicKey(msg, state);
                        chatUI.printInfo("[보안 연결 완료]");
                        chatUI.printPublicKey(state.getTrustedPeerId(), state.getTrustedFingerprint());
                        break;

                    case CHAT: // 누군가 나에게 채팅을 보냈을 때
                        if (state.isKeyReady()) {
                            // 암호화된 메시지를 복호화해서 화면에 출력
                            String plain = chatService.decryptChat(state, msg);
                            chatUI.printPeerChat(msg.getSentAt(), msg.getSender(), plain);
                        }
                        break;

                    case ROOM_DESTROYED: // 방 폭파
                        chatUI.printInfo("[안내] 채팅방이 폭파되었습니다.");
                        state.clearRoom(); // 내 상태 초기화
                        return;

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            if (state.isInRoom()) {
                chatUI.printInfo("[수신 스레드 종료]");
            }
        }
    }
}