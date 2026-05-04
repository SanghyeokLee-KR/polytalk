package com.polytalk.client.factory;

import com.polytalk.client.controller.AuthFlowController;
import com.polytalk.client.controller.RoomFlowController;
import lombok.Getter;

/**
 * 클라이언트에서 사용하는 주요 객체들을 묶어두는 클래스
 */
@Getter
public class ClientContext {

    private final AuthFlowController authFlowController;
    private final RoomFlowController roomFlowController;

    public ClientContext(
            AuthFlowController authFlowController,
            RoomFlowController roomFlowController
    ) {
        this.authFlowController = authFlowController;
        this.roomFlowController = roomFlowController;
    }
}