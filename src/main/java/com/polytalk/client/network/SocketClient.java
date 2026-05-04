package com.polytalk.client.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 소켓 클라이언트
 */
public class SocketClient implements AutoCloseable {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public SocketClient(String host, int port) throws IOException {
        // 서버 주소랑 포트번호로 연결 시작
        this.socket = new Socket(host, port);

        // 이거 없음 무한대기 0.5초
        this.socket.setSoTimeout(500);

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public BufferedReader reader() {
        return reader;
    }

    public PrintWriter writer() {
        return writer;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}