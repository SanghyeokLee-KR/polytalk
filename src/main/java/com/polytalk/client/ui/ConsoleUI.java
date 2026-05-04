package com.polytalk.client.ui;

import java.util.Scanner;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);

    public String input(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    public void print(String message) {
        System.out.println(message);
    }

    public void blank() {
        System.out.println();
    }
}
