package com.polytalk.client.controller;

import com.polytalk.client.service.ClientAuthService;
import com.polytalk.client.state.ClientState;
import com.polytalk.client.ui.AuthConsoleUI;
import com.polytalk.client.ui.ConsoleUI;

/**
 * 로그인과 회원가입 흐름을 관리하는 입구 컨트롤러
 */
public class AuthFlowController {

    private final ClientAuthService authService;
    private final AuthConsoleUI authUI;
    private final ConsoleUI consoleUI;

    public AuthFlowController(ClientAuthService authService, AuthConsoleUI authUI, ConsoleUI consoleUI) {
        this.authService = authService;
        this.authUI = authUI;
        this.consoleUI = consoleUI;
    }

    public ClientState run(String clientName) throws Exception {
        while (true) {
            // 1. 메뉴 출력 및 공통 입력(ID, PW) 받기
            String menu = authUI.printMainMenu(clientName);

            if (menu.equals("0")) return null; // 0번은 바로 종료

            String userId = authUI.inputId();
            String password = authUI.inputPassword();

            // 2. 선택한 메뉴에 따른 로직 분기
            switch (menu) {
                case "1": // 로그인
                    ClientState state = authService.login(userId, password);
                    if (state != null) {
                        consoleUI.print("[로그인 성공]");
                        return state; // 성공하면 유저 상태를 들고 메인으로
                    }
                    consoleUI.print("[로그인 실패] 계정 정보가 틀렸거나 인증키가 없습니다.");
                    break;

                case "2": // 회원가입
                    boolean success = authService.signUp(userId, password);
                    consoleUI.print(success ? "[회원가입 완료] 로그인 해주세요." : "[회원가입 실패]");
                    break;

                default:
                    consoleUI.print("[오류] 잘못된 메뉴 번호입니다.");
                    break;
            }
        }
    }
}