package com.kyobo.server.controller;

import java.util.Scanner;

import com.kyobo.server.entity.User;
import com.kyobo.server.service.UserService;
import com.kyobo.server.service.UserService.WithdrawResult;
import org.apache.ibatis.exceptions.PersistenceException;

public class UserController {
    private final Scanner scanner;
    private final UserService userService;

    public UserController(Scanner scanner) {
        this(scanner, new UserService());
    }

    public UserController(Scanner scanner, UserService userService) {
        this.scanner = scanner;
        this.userService = userService;
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

    /** @return 탈퇴 완료 또는 유효하지 않은 회원이면 true (세션 해제 필요) */
    public boolean runWithdraw(User currentUser) {
        if (currentUser == null) {
            System.out.println("로그인이 필요합니다.");
            return false;
        }
        System.out.println("===========================================================");
        System.out.println("회원 탈퇴");
        System.out.println("-----------------------------------------------------------");
        try {
            WithdrawResult result = userService.checkWithdraw(currentUser.getUserId());
            if (result != WithdrawResult.READY) {
                return printWithdrawResult(result);
            }
            while (true) {
                String password = readLine("[PASSWORD] : ");
                if (isBlank(password)) {
                    printWithdrawResult(WithdrawResult.PASSWORD_REQUIRED);
                    continue;
                }
                result = userService.verifyWithdraw(currentUser.getUserId(), password);
                if (result == WithdrawResult.READY) {
                    String answer = readLine("정말 탈퇴하시겠습니까? (탈퇴: 0, 취소: 1) ");
                    if (!"0".equals(answer.trim())) {
                        System.out.println("회원 탈퇴를 취소했습니다.");
                        return false;
                    }
                    // 최종 확인 중 변경된 회원 상태와 예매 내역도 다시 검증한다.
                    result = userService.withdraw(currentUser.getUserId(), password);
                }
                boolean logout = printWithdrawResult(result);
                if (result != WithdrawResult.PASSWORD_REQUIRED && result != WithdrawResult.PASSWORD_MISMATCH) {
                    return logout;
                }
            }
        } catch (PersistenceException e) {
            System.out.println("회원 탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            return false;
        }
    }

    private boolean printWithdrawResult(WithdrawResult result) {
        switch (result) {
            case SUCCESS:
                System.out.println("회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.");
                return true;
            case INVALID_USER:
                System.out.println("유효하지 않은 회원정보입니다.");
                return true;
            case ACTIVE_BOOKINGS:
                System.out.println("예매 내역이 존재합니다. 예매를 먼저 취소해주세요.");
                break;
            case PASSWORD_REQUIRED:
                System.out.println("비밀번호를 입력해 주세요.");
                break;
            case PASSWORD_MISMATCH:
                System.out.println("비밀번호가 일치하지 않습니다. 다시 입력해 주세요.");
                break;
            default:
                break;
        }
        return false;
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
