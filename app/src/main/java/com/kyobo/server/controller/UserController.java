package com.kyobo.server.controller;

import java.util.InputMismatchException;
import java.util.Scanner;

import com.kyobo.server.entity.User;
import com.kyobo.server.service.UserService;

public class UserController {
    private final Scanner scanner;
    private final UserService userService;

    public UserController(Scanner scanner) {
        this.scanner = scanner;
        this.userService = new UserService();
    }

    /**
     * @return false면 프로그램 종료 요청
     */
    public boolean runSignUp() {
        while (true) {
            try {
                System.out.println("===========================================================");
                System.out.println("회원가입");
                System.out.println("-----------------------------------------------------------");
                String loginId = readLine("[ID] : ");
                String password = readLine("[PASSWORD] : ");
                String passwordConfirm = readLine("[PASSWORD 확인] : ");
                String name = readLine("[이름] : ");
                String phone = readLine("[전화번호] : ");
                System.out.println("===========================================================");

                User user = userService.signUp(loginId, password, passwordConfirm, name, phone);
                System.out.println("회원가입이 완료되었습니다. 환영합니다, " + user.getName() + "님!");
                return true;
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                int selected = askRetryMenu();
                if (selected == 1) {
                    continue;
                }
                if (selected == 2) {
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                int selected = askRetryMenu();
                if (selected == 1) {
                    continue;
                }
                if (selected == 2) {
                    return true;
                }
                return false;
            }
        }
    }

    /** 0(종료), 1(다시 입력), 2(뒤로가기)만 반환 */
    private int askRetryMenu() {
        while (true) {
            try {
                System.out.println("[1. 다시 입력하기] [2. 뒤로가기] [0. 종료하기]");
                int selected = readInt("기능 선택: ");
                if (selected == 0 || selected == 1 || selected == 2) {
                    return selected;
                }
                System.out.println("존재하지 않는 기능입니다.");
            } catch (InputMismatchException e) {
                System.out.println("숫자만 입력해 주세요.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
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
