package com.polytalk.server.factory;

import com.polytalk.controller.MemberController;
import com.polytalk.server.ClientManager;
import com.polytalk.server.ServerMessageRouter;
import com.polytalk.server.repository.ChatLogRepository;
import com.polytalk.server.repository.ChatRoomRepository;
import com.polytalk.server.repository.MemberRepository;
import com.polytalk.server.service.HandshakeService;
import com.polytalk.server.service.PublicKeyService;
import com.polytalk.server.service.RoomKeyExchangeService;
import com.polytalk.server.service.RoomPasswordService;
import com.polytalk.server.service.ServerAuthService;
import com.polytalk.server.service.ServerChatService;
import com.polytalk.server.service.ServerHandshakeService;
import com.polytalk.server.service.ServerRoomService;
import com.polytalk.service.MemberService;

/**
 * 서버 실행에 필요한 객체를 생성하는 클래스.
 *
 * ChatServer에 객체 생성 코드가 너무 많아지는 것을 막기 위해
 * Factory로 분리했다.
 */
public class ServerFactory {

    /**
     * 서버에서 사용할 객체들을 생성한 뒤 Context로 묶어서 반환한다.
     */
    public static ServerContext create() {

        // 데이터 저장소
        MemberRepository memberRepository = new MemberRepository();
        ChatRoomRepository chatRoomRepository = new ChatRoomRepository();
        ChatLogRepository chatLogRepository = new ChatLogRepository();

        // 회원 기능
        MemberService memberService = new MemberService(memberRepository);
        MemberController memberController = new MemberController(memberService);

        // 접속 클라이언트 관리
        ClientManager clientManager = new ClientManager();

        // 공개키, 방 비밀번호, 방 키 교환 관련 기능
        PublicKeyService publicKeyService =
                new PublicKeyService(clientManager);

        RoomPasswordService roomPasswordService =
                new RoomPasswordService();

        RoomKeyExchangeService roomKeyExchangeService =
                new RoomKeyExchangeService(publicKeyService);

        // 서버 기능별 서비스
        ServerAuthService authService =
                new ServerAuthService(memberController, publicKeyService);

        ServerRoomService roomService =
                new ServerRoomService(
                        chatRoomRepository,
                        chatLogRepository,
                        clientManager,
                        roomPasswordService,
                        roomKeyExchangeService
                );

        ServerChatService chatService =
                new ServerChatService(chatLogRepository, clientManager);

        // 핸드셰이크 구현체는 인터페이스 타입으로 받는다.
        HandshakeService handshakeService =
                new ServerHandshakeService();

        // 클라이언트 요청을 기능별 서비스로 나누어 처리하는 Router
        ServerMessageRouter router =
                new ServerMessageRouter(
                        authService,
                        roomService,
                        chatService,
                        handshakeService
                );

        return new ServerContext(router, clientManager);
    }
}