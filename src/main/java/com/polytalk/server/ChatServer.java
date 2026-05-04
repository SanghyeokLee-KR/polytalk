package com.polytalk.server;

import com.polytalk.server.factory.ServerContext;
import com.polytalk.server.factory.ServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PolyTalk 서버 실행 클래스.
 */
@Slf4j
public class ChatServer {

    private static final int PORT = 5000;

    // 여러 클라이언트를 동시에 처리하기 위해 스레드 풀 사용
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {

        // 서버에서 필요한 객체들은 Factory에서 생성한다.
        ServerContext context = ServerFactory.create();

        ServerMessageRouter router = context.getRouter();
        ClientManager clientManager = context.getClientManager();

        log.info("PolyTalk 서버 시작. port={}", PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                // 클라이언트 접속 대기
                Socket socket = serverSocket.accept();

                // 클라이언트 한 명을 담당할 Handler 생성
                ClientHandler handler =
                        new ClientHandler(socket, router, clientManager);

                // 접속 클라이언트 등록
                clientManager.add(handler);

                // Handler를 별도 스레드에서 실행
                pool.submit(handler);

                log.info("클라이언트 접속. 현재 접속 수={}", clientManager.size());
            }

        } catch (IOException e) {
            log.error("서버 실행 중 오류 발생", e);
        }
    }
}