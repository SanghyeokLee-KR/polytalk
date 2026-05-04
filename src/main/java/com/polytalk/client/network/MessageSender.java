package com.polytalk.client.network;

import com.polytalk.protocol.JsonUtil;
import com.polytalk.protocol.Message;

import java.io.PrintWriter;

/**
 * 서버로 메시지를 전송하는 클래스
 */
public class MessageSender {

    private final PrintWriter writer;

    public MessageSender(PrintWriter writer) {
        this.writer = writer;
    }

    public void send(Message message) {
        writer.println(JsonUtil.toJson(message));
    }
}
