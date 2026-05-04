package com.polytalk.client.ui;

public class AuthConsoleUI {

    private final ConsoleUI ui;

    public AuthConsoleUI(ConsoleUI ui) {
        this.ui = ui;
    }

    public String printMainMenu(String clientName) {
        ui.blank();
        ui.print("================================");
        ui.print(" PolyTalk " + clientName);
        ui.print("================================");
        ui.print("1. 로그인");
        ui.print("2. 회원가입");
        ui.print("0. 종료");
        return ui.input("선택: ");
    }

    public String inputId() {
        return ui.input("ID 입력: ");
    }

    public String inputPassword() {
        return ui.input("비밀번호 입력: ");
    }
}
