package com.kyobo.server.controller;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ConsoleController {
    private final Scanner scanner;
    private final UserController userController;
    private boolean programRunning = true;

    public ConsoleController(Scanner scanner) {
        this.scanner = scanner;
        this.userController = new UserController(scanner);
    }

    public void run() {
        welcome();
        runGuestHome();
    }

    private void welcome() {
        System.out.println("================================================");
        System.out.println("교보시네마에 오신 것을 환영합니다");
        System.out.println("================================================");
    }

    private void runGuestHome() {
        while (programRunning) {
            try {
                System.out.println("원하시는 기능을 선택해주세요.");
                System.out.println("[1. 영화 목록 조회] [2. 로그인] [3. 회원가입] [0. 종료]");
                int selected = readInt("기능 선택: ");
                switch (selected) {
                    case 1:
                        // TODO: 영화 목록 조회 구현 후 수정
                        System.out.println("영화 목록 조회 구현 필요");
                        break;
                    case 2:
                        // TODO: 로그인 구현 후 수정
                        System.out.println("로그인 구현 필요");
                        break;
                    case 3:
                        if (!userController.runSignUp()) {
                            programRunning = false;
                        }
                        break;
                    case 0:
                        requestExit();
                        break;
                    default:
                        System.out.println("존재하지 않는 기능입니다.");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("숫자만 입력해 주세요.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    private void requestExit() {
        if (confirmExit()) {
            programRunning = false;
        }
    }

    private boolean confirmExit() {
        System.out.print("정말 종료하시겠습니까? (종료: 0, 취소: 1) ");
        int answer = readInt("");
        if (answer == 0) {
            System.out.println("================================================");
            System.out.println("교보시네마를 종료합니다. 안녕히 가세요 👋");
            System.out.println("================================================");
            return true;
        }
        return false;
    }

    private int readInt(String prompt) {
        if (!prompt.isBlank()) {
            System.out.print(prompt);
        }
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            throw new InputMismatchException();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }
}
