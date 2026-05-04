package com.polytalk.client.ui;

public class ChatConsoleUI {

    private final ConsoleUI ui;

    public ChatConsoleUI(ConsoleUI ui) {
        this.ui = ui;
    }

    public void printChatHeader(String roomName, int memberCount) {
        ui.blank();
        ui.print("================================");
        ui.print(" 채팅방: " + roomName + " (" + memberCount + "/2)");
        ui.print("================================");
        ui.print("명령어: /finger, /방폭파, /나가기");
        ui.print("--------------------------------");
    }

    public String inputMessage(String userId) {
        return ui.input(userId + " : ");
    }

    public void printMyChat(String sentAt, String userId, String plainText) {
        ui.print("[" + sentAt + "] " + userId + " : " + plainText);
    }

    public void printPeerChat(String sentAt, String sender, String plainText) {
        ui.blank();
        ui.print("[" + sentAt + "] " + sender + " : " + plainText);
    }

    public void printPublicKey(String peerId, String fingerprint) {
        ui.blank();
        ui.print("[상대방 공개키]");
        ui.print("상대방 ID: " + peerId);
        ui.print("Fingerprint: " + fingerprint);
    }

    public void printHistoryTitle() {
        ui.blank();
        ui.print("========== 이전 대화내역 ==========");
    }

    public void printHistoryLine(String sentAt, String sender, String text) {
        ui.print("[" + sentAt + "] " + sender + " : " + text);
    }

    public void printInfo(String message) {
        ui.print(message);
    }
}