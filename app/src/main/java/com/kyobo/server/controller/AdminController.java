package com.kyobo.server.controller;

import java.util.Scanner;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.entity.Admin;
import com.kyobo.server.service.AdminService;

public class AdminController {
    private final Scanner scanner;
    private final AdminService adminService;

    public AdminController(Scanner scanner) {
        this.scanner = scanner;
        this.adminService = new AdminService();
    }

    /**
     * 메뉴 선택 등 일반 입력 자리에서 관리자 코드가 입력됐는지 가로채 확인한다.
     * @return 관리자 코드가 아니면 false (호출한 쪽이 원래 입력 처리를 계속하면 됨).
     *         관리자 코드였다면 인증 성공/실패/취소와 무관하게 true.
     */
    public boolean tryEnterAdminMode(String input) {
        ApiResponse<Admin> findResponse = adminService.findAdminByCode(input);
        if (!"00".equals(findResponse.getStatusCode())) {
            return false;
        }
        Admin admin = findResponse.getData();

        String password = readLine("관리자 비밀번호 입력 (0: 취소): ");
        if (password.equals("0")) {
            System.out.println("관리자 인증을 취소합니다.");
            return true;
        }

        ApiResponse<Void> verifyResponse = adminService.verifyAdminPassword(password, admin.getAdminPassword());
        if (!"00".equals(verifyResponse.getStatusCode())) {
            System.out.println(verifyResponse.getStatusMessage());
            return true;
        }

        System.out.println("[" + admin.getCinemaName() + "] 관리자 모드로 진입합니다.");
        runAdminMenu(admin);
        return true;
    }

    private void runAdminMenu(Admin admin) {
        boolean inAdminMode = true;
        while (inAdminMode) {
            System.out.println();
            System.out.println("===== [" + admin.getCinemaName() + "] 관리자 모드 =====");
            System.out.println("[1. 영화 관리] [2. 상영관 관리] [3. 상영회차 관리] [0. 종료]");
            String choice = readLine("선택: ");
            switch (choice) {
                case "1":
                    manageMovies();
                    break;
                case "2":
                    manageScreens();
                    break;
                case "3":
                    manageShowtimes();
                    break;
                case "0":
                    System.out.println("관리자 모드를 종료합니다.");
                    inAdminMode = false;
                    break;
                default:
                    System.out.println("올바른 번호를 입력하세요.");
            }
        }
    }

    private void manageMovies() {
        // TODO: 담당자 구현 예정
        System.out.println("[영화 관리] 기능은 아직 구현되지 않았습니다.");
    }

    private void manageScreens() {
        // TODO: 담당자 구현 예정
        System.out.println("[상영관 관리] 기능은 아직 구현되지 않았습니다.");
    }

    private void manageShowtimes() {
        // TODO: 담당자 구현 예정
        System.out.println("[상영회차 관리] 기능은 아직 구현되지 않았습니다.");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
