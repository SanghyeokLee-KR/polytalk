package com.polytalk.server;

import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 접속 중인 클라이언트를 관리하는 클래스
 * 단위 브로드캐스트를 담당
 */
@Getter
public class ClientManager {

    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    // 클라이언트 추가
    public void add(ClientHandler client) {
        clients.add(client);
    }

    // 클라이언트 삭제
    public void remove(ClientHandler client) {
        clients.remove(client);
    }

    // 클라이언트 수
    public int size() {
        return clients.size();
    }

    // 같은 방에 있는 클라이언트에게만 전송
    public void broadcastRoom(String json, String roomId, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && roomId.equals(client.getCurrentRoomId())) {
                client.send(json);
            }
        }
    }
}