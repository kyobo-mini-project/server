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
        String ageInput = readLine("[나이] : ");
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
            if (!isValidPassword(password)) {
                System.out.println("비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다.");
                password = readLine("[PASSWORD] : ");
                passwordConfirm = readLine("[PASSWORD 확인] : ");
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
            Integer age = parseAge(ageInput);
            if (age == null) {
                System.out.println("나이는 0 이상의 숫자로 입력해 주세요.");
                ageInput = readLine("[나이] : ");
                continue;
            }
            if (isBlank(phone)) {
                System.out.println("전화번호를 입력해 주세요.");
                phone = readLine("[전화번호] : ");
                continue;
            }

            try {
                userService.signUp(loginId, password, name, age, phone);
                System.out.println("회원가입이 완료되었습니다!");
                return true;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                password = readLine("[PASSWORD] : ");
                passwordConfirm = readLine("[PASSWORD 확인] : ");
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage() + " 다른 아이디를 입력해 주세요.");
                loginId = readLine("[ID] : ");
            }
        }
    }

    /**
     * @return 로그인 성공한 사용자 (이름은 복호화된 평문)
     */
    public User runSignIn() {
        System.out.println("===========================================================");
        System.out.println("로그인");
        System.out.println("-----------------------------------------------------------");

        String loginId = readLine("[ID] : ");
        String password = readLine("[PASSWORD] : ");
        System.out.println("===========================================================");

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

            try {
                User user = userService.signIn(loginId, password);
                System.out.println("로그인 성공! 환영합니다, " + user.getName() + "님!");
                return user;
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage() + " 다시 입력해 주세요.");
                loginId = readLine("[ID] : ");
                password = readLine("[PASSWORD] : ");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 영문·숫자 각각 1자 이상, 총 8자 이상 */
    private boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
    }

    /** @return 유효하지 않으면 null */
    private Integer parseAge(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            int age = Integer.parseInt(value.trim());
            return age >= 0 ? age : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
