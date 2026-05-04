package com.polytalk.client.controller;

import com.polytalk.client.service.ClientRoomService;
import com.polytalk.client.state.ClientState;
import com.polytalk.client.ui.ConsoleUI;
import com.polytalk.client.ui.RoomConsoleUI;
import com.polytalk.domain.ChatRoom;

import java.util.List;

/**
 * 로그인 후 방 목록 보기, 생성, 입장 등 '방 관리' 흐름을 제어
 */
public class RoomFlowController {

    private final ClientRoomService roomService;
    private final RoomConsoleUI roomUI;
    private final ConsoleUI consoleUI;
    private final ChatRoomController chatRoomController;

    public RoomFlowController(ClientRoomService roomService, RoomConsoleUI roomUI, ConsoleUI consoleUI, ChatRoomController chatRoomController) {
        this.roomService = roomService;
        this.roomUI = roomUI;
        this.consoleUI = consoleUI;
        this.chatRoomController = chatRoomController;
    }

    /**
     * 로그아웃 전까지 방 메뉴를 무한 반복해서 보여줌
     */
    public void run(ClientState state) throws Exception {
        while (state.isRunning()) {
            String menu = roomUI.printRoomMenu();

            switch (menu) {
                case "1" -> printRoomList(state); // 방 목록 출력
                case "2" -> createRoom(state);    // 새 방 만들기
                case "3" -> joinRoom(state);      // 기존 방 들어가기
                case "4" -> {
                    roomService.logout(state);    // 로그아웃 처리
                    return;
                }
                default -> consoleUI.print("[오류] 잘못된 메뉴입니다.");
            }
        }
    }

    // 서버에 저장된 전체 방 목록을 가져와서 UI에 뿌림
    private void printRoomList(ClientState state) throws Exception {
        List<ChatRoom> rooms = roomService.findRooms(state);
        roomUI.printRooms(rooms);
    }

    /**
     * 방 생성 로직: 이름과 비번을 입력받아 방을 만들고 바로 입장함
     */
    private void createRoom(ClientState state) throws Exception {
        String roomName = roomUI.inputRoomName();
        if (roomName.isBlank()) {
            consoleUI.print("[오류] 방 이름은 비어 있을 수 없습니다.");
            return;
        }

        String roomPassword = roomUI.inputRoomPassword();
        if (roomPassword.isBlank()) {
            consoleUI.print("[오류] 방 비밀번호는 비어 있을 수 없습니다.");
            return;
        }

        // 서버에 방 생성 요청
        ChatRoom room = roomService.createRoom(state, roomName, roomPassword);

        if (room == null) {
            consoleUI.print("[방 생성 실패]");
            return;
        }

        // 방이 만들어지면 자동으로 해당 방 채팅으로 넘어감
        chatRoomController.run(state, room);
    }

    /**
     * 방 입장 로직: 번호 선택 -> 비번 입력 -> 입장 시도
     */
    private void joinRoom(ClientState state) throws Exception {
        List<ChatRoom> rooms = roomService.findRooms(state);
        roomUI.printRooms(rooms);

        if (rooms.isEmpty()) return;

        String input = roomUI.inputRoomNumber();
        if (input.equals("0")) return; // 취소

        int index;
        try {
            index = Integer.parseInt(input) - 1; // 사용자는 1번부터 보니까 -1 해줌
        } catch (NumberFormatException e) {
            consoleUI.print("[오류] 숫자를 입력하세요.");
            return;
        }

        if (index < 0 || index >= rooms.size()) {
            consoleUI.print("[오류] 잘못된 번호입니다.");
            return;
        }

        // 입장할 때도 방 비밀번호 확인이 필요함
        String roomPassword = roomUI.inputRoomPassword();
        if (roomPassword.isBlank()) {
            consoleUI.print("[오류] 방 비밀번호는 비어 있을 수 없습니다.");
            return;
        }

        // 서버에 입장 요청
        ChatRoom room = roomService.joinRoom(state, rooms.get(index).getRoomId(), roomPassword);

        if (room == null) {
            consoleUI.print("[입장 실패] 방이 가득 찼거나 비밀번호가 틀렸습니다.");
            return;
        }

        // 입장 성공 시 채팅 컨트롤러로 제어권 넘김
        chatRoomController.run(state, room);
    }
}