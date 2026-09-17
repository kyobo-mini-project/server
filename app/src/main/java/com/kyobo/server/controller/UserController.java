package com.kyobo.server.controller;

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
        System.out.println("===========================================================");
        System.out.println("회원가입");
        System.out.println("-----------------------------------------------------------");

        // 1차: 항목을 끝까지 한 번씩 입력받는다 (빈 값이어도 다음으로 넘어감)
        String loginId = readLine("[ID] : ");
        String password = readLine("[PASSWORD] : ");
        String passwordConfirm = readLine("[PASSWORD 확인] : ");
        String name = readLine("[이름] : ");
        String phone = readLine("[전화번호] : ");
        System.out.println("===========================================================");

        // 2차: 문제 있는 항목만 다시 받고, 가입 시도 (아이디 중복은 이때 DB 조회)
        while (true) {
            if (isBlank(loginId)) {
                System.out.println("아이디를 입력해 주세요.");
                loginId = readLine("[ID] : ");
                continue;
            }
            if (isBlank(password)) {
                System.out.println("비밀번호를 입력해 주세요.");
                password = readLine("[PASSWORD] : ");
                continue;
            }
            if (isBlank(passwordConfirm)) {
                System.out.println("비밀번호 확인을 입력해 주세요.");
                passwordConfirm = readLine("[PASSWORD 확인] : ");
                continue;
            }
            if (!password.equals(passwordConfirm)) {
                System.out.println("비밀번호와 비밀번호 확인이 일치하지 않습니다. 다시 입력해 주세요.");
                password = readLine("[PASSWORD] : ");
                passwordConfirm = readLine("[PASSWORD 확인] : ");
                continue;
            }
            if (isBlank(name)) {
                System.out.println("이름을 입력해 주세요.");
                name = readLine("[이름] : ");
                continue;
            }
            if (isBlank(phone)) {
                System.out.println("전화번호를 입력해 주세요.");
                phone = readLine("[전화번호] : ");
                continue;
            }

            try {
                User user = userService.signUp(loginId, password, name, phone);
                System.out.println("회원가입이 완료되었습니다. 환영합니다, " + user.getName() + "님!");
                return true;
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage() + " 다른 아이디를 입력해 주세요.");
                loginId = readLine("[ID] : ");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
