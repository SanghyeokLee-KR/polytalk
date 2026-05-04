package com.polytalk.client.factory;

import com.polytalk.client.controller.AuthFlowController;
import com.polytalk.client.controller.ChatRoomController;
import com.polytalk.client.controller.RoomFlowController;
import com.polytalk.client.network.MessageSender;
import com.polytalk.client.network.ServerResponseReader;
import com.polytalk.client.network.SocketClient;
import com.polytalk.client.service.ClientAuthService;
import com.polytalk.client.service.ClientChatService;
import com.polytalk.client.service.ClientRoomService;
import com.polytalk.client.service.ClientSecurityService;
import com.polytalk.client.ui.AuthConsoleUI;
import com.polytalk.client.ui.ChatConsoleUI;
import com.polytalk.client.ui.ConsoleUI;
import com.polytalk.client.ui.RoomConsoleUI;

/**
 * 클라이언트 객체를 생성해서 연결해 주는 Factory
 */

public class ClientFactory {

    public static ClientContext create(
            SocketClient socketClient,
            ConsoleUI consoleUI
    ) {

        // 서버로 메시지를 보내는 객체
        MessageSender sender =
                new MessageSender(socketClient.writer());

        // 상대방 공개키 처리와 AES 키 생성을 담당
        ClientSecurityService securityService =
                new ClientSecurityService();

        // 서버 응답을 읽는 객체
        ServerResponseReader responseReader =
                new ServerResponseReader(
                        socketClient.reader(),
                        securityService
                );

        // 기능별 서비스
        ClientAuthService authService =
                new ClientAuthService(sender, responseReader);

        ClientRoomService roomService =
                new ClientRoomService(sender, responseReader);

        ClientChatService chatService =
                new ClientChatService(sender);

        // 콘솔 UI
        AuthConsoleUI authUI =
                new AuthConsoleUI(consoleUI);

        RoomConsoleUI roomUI =
                new RoomConsoleUI(consoleUI);

        ChatConsoleUI chatUI =
                new ChatConsoleUI(consoleUI);

        // 채팅방 내부 흐름
        ChatRoomController chatRoomController =
                new ChatRoomController(
                        socketClient,
                        roomService,
                        chatService,
                        securityService,
                        chatUI
                );

        // 방 목록, 생성, 입장 흐름
        RoomFlowController roomFlowController =
                new RoomFlowController(
                        roomService,
                        roomUI,
                        consoleUI,
                        chatRoomController
                );

        // 로그인, 회원가입 흐름
        AuthFlowController authFlowController =
                new AuthFlowController(
                        authService,
                        authUI,
                        consoleUI
                );

        return new ClientContext(
                authFlowController,
                roomFlowController
        );
    }
}