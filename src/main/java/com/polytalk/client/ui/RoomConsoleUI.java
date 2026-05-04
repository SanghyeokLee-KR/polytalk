package com.polytalk.client.ui;

import com.polytalk.domain.ChatRoom;

import java.util.List;

public class RoomConsoleUI {

    private final ConsoleUI ui;

    public RoomConsoleUI(ConsoleUI ui) {
        this.ui = ui;
    }

    public String printRoomMenu() {
        ui.blank();
        ui.print("================================");
        ui.print(" PolyTalk 채팅방 메뉴");
        ui.print("================================");
        ui.print("1. 채팅방 목록 보기");
        ui.print("2. 채팅방 생성");
        ui.print("3. 채팅방 접속");
        ui.print("4. 로그아웃");
        return ui.input("선택: ");
    }

    public void printRooms(List<ChatRoom> rooms) {
        ui.blank();
        ui.print("========== 채팅방 목록 ==========");

        if (rooms.isEmpty()) {
            ui.print("생성된 채팅방이 없습니다.");
            return;
        }

        for (int i = 0; i < rooms.size(); i++) {
            ChatRoom room = rooms.get(i);
            ui.print((i + 1) + ". " + room.getRoomName() + " (" + room.getMemberCount() + "/2)");
        }
    }

    public String inputRoomName() {
        return ui.input("채팅방 이름 입력: ");
    }

    public String inputRoomPassword() {
        return ui.input("채팅방 비밀번호 입력: ");
    }

    public String inputRoomNumber() {
        return ui.input("접속할 방 번호 선택, 0. 뒤로가기: ");
    }
}