package com.polytalk.server;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 클라이언트 1명을 처리하는 클래스
 * 소켓으로부터 메시지를 읽어서 Router로 전달하는 역할
 */
@Getter
@Setter
@Slf4j
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerMessageRouter router;
    private final ClientManager clientManager;

    private BufferedReader reader;
    private PrintWriter writer;

    private String userId;
    private String currentRoomId;

    public ClientHandler(
            Socket socket,
            ServerMessageRouter router,
            ClientManager clientManager
    ) {
        this.socket = socket;
        this.router = router;
        this.clientManager = clientManager;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );

            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                    true
            );

            String line;

            // 메시지를 계속 읽어서 Router로 넘김
            while ((line = reader.readLine()) != null) {
                router.route(line, this);
            }

        } catch (IOException e) {
            log.info("클라이언트 연결 종료");
        } finally {
            close();
        }
    }

    // 클라이언트에게 메시지 전송
    public void send(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    // 연결 종료 처리
    public void close() {
        clientManager.remove(this);

        try {
            socket.close();
        } catch (IOException e) {
            log.warn("소켓 종료 실패", e);
        }
    }
}