package com.polytalk.client;

import com.polytalk.client.controller.AuthFlowController;
import com.polytalk.client.controller.RoomFlowController;
import com.polytalk.client.factory.ClientContext;
import com.polytalk.client.factory.ClientFactory;
import com.polytalk.client.network.SocketClient;
import com.polytalk.client.state.ClientState;
import com.polytalk.client.ui.ConsoleUI;
import lombok.extern.slf4j.Slf4j;

/**
 * PolyTalk 클라이언트 실행 클래스
 * <p>
 * 서버에 연결한 뒤 로그인 메뉴 채팅방 메뉴 실행
 */
@Slf4j
public class ClientApp {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void start(String clientName) {
        ConsoleUI consoleUI = new ConsoleUI();

        while (true) {
            try (SocketClient socketClient = new SocketClient(HOST, PORT)) {

                // 클라이언트에서 사용할 객체들은 Factory에서 생성
                ClientContext context = ClientFactory.create(socketClient, consoleUI);

                AuthFlowController authFlowController = context.getAuthFlowController();

                RoomFlowController roomFlowController = context.getRoomFlowController();

                // 로그인 또는 회원가입
                ClientState state = authFlowController.run(clientName);

                if (state == null) {
                    return;
                }

                // 로그인 성공 후 채팅방 메뉴로 이동
                roomFlowController.run(state);

            } catch (Exception e) {
                log.error("클라이언트 실행 중 오류 발생", e);
                consoleUI.print("[오류] " + e.getMessage());
            }
        }
    }
}