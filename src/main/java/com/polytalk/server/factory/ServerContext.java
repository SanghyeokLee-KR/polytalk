package com.polytalk.server.factory;

import com.polytalk.server.ClientManager;
import com.polytalk.server.ServerMessageRouter;

/**
 * 서버 실행에 필요한 핵심 객체를 묶어두는 클래스.
 *
 * Factory에서 Router와 ClientManager를 같이 반환해야 해서
 * Context 객체로 묶었다.
 */
public class ServerContext {

    private final ServerMessageRouter router;
    private final ClientManager clientManager;

    public ServerContext(
            ServerMessageRouter router,
            ClientManager clientManager
    ) {
        this.router = router;
        this.clientManager = clientManager;
    }

    public ServerMessageRouter getRouter() {
        return router;
    }

    public ClientManager getClientManager() {
        return clientManager;
    }
}