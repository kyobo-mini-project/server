package com.kyobo.server.controller;

import java.util.InputMismatchException;
import java.util.Scanner;

import com.kyobo.server.common.GoHomeSignal;
import com.kyobo.server.common.RequireLoginSignal;
import com.kyobo.server.entity.User;

public class ConsoleController {
    /** readMenuChoice가 관리자 코드로 처리했을 때 반환하는 값 (메뉴 번호와 겹치지 않음) */
    private static final int ADMIN_MODE_HANDLED = -1;

    private final Scanner scanner;
    private final UserController userController;
    private final MovieController movieController;
    private final AdminController adminController;
    private boolean programRunning = true;
    private User currentUser;

    public ConsoleController(Scanner scanner) {
        this(scanner, new UserController(scanner), new MovieController(scanner), new AdminController(scanner));
    }

    public ConsoleController(Scanner scanner, UserController userController,
                             MovieController movieController, AdminController adminController) {
        this.scanner = scanner;
        this.userController = userController;
        this.movieController = movieController;
        this.adminController = adminController;
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
                int selected = readMenuChoice("기능 선택: ");
                if (selected == ADMIN_MODE_HANDLED) {
                    System.out.println();
                    continue;
                }
                switch (selected) {
                    case 1:
                        programRunning = movieController.runMovieList(currentUser);
                        break;
                    case 2:
                        loginAndEnterUserHome();
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
            } catch (RequireLoginSignal e) {
                loginAndEnterUserHome();
            }
            System.out.println();
        }
    }

    private void loginAndEnterUserHome() {
        currentUser = userController.runSignIn();
        runUserHome();
    }

    private void runUserHome() {
        while (programRunning && currentUser != null) {
            try {
                System.out.println("원하시는 기능을 선택해주세요. (" + currentUser.getName() + "님)");
                System.out.println("[1. 영화 목록 조회] [2. 예매 내역] [3. 로그아웃] [4. 회원 탈퇴] [0. 종료]");
                int selected = readMenuChoice("기능 선택: ");
                if (selected == ADMIN_MODE_HANDLED) {
                    System.out.println();
                    continue;
                }
                switch (selected) {
                    case 1:
                        programRunning = movieController.runMovieList(currentUser);
                        break;
                    case 2:
                        
                        new BookingController(scanner).runHistory(currentUser);
                        break;
                    case 3:
                        requestLogout();
                        break;
                    case 4:
                        if (userController.runWithdraw(currentUser)) {
                            currentUser = null;
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
            } catch (GoHomeSignal e) {
                // 예매 등 중첩 화면에서 홈으로 복귀 확정 - 별도 처리 없이 메인 메뉴 루프 계속
            }
            System.out.println();
        }
    }

    private void requestLogout() {
        if (confirmLogout()) {
            System.out.println("로그아웃되었습니다.");
            currentUser = null;
        }
    }

    private boolean confirmLogout() {
        System.out.print("정말 로그아웃하시겠습니까? (로그아웃: 0, 취소: 1) ");
        int answer = readInt("");
        return answer == 0;
    }

    private void requestExit() {
        if (confirmExit()) {
            programRunning = false;
            currentUser = null;
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

    /**
     * 메뉴 번호를 입력받되, 먼저 관리자 코드인지 가로채 확인한다.
     * 관리자 코드로 처리됐으면 ADMIN_MODE_HANDLED를 반환한다.
     */
    private int readMenuChoice(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        if (adminController.tryEnterAdminMode(input)) {
            return ADMIN_MODE_HANDLED;
        }
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new InputMismatchException();
        }
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
