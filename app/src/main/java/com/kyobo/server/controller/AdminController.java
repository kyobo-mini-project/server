package com.kyobo.server.controller;

import java.util.Scanner;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.entity.Admin;
import com.kyobo.server.service.AdminService;
import java.util.List;
import com.kyobo.server.entity.Room;

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
                    manageScreens(admin);
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

    private void manageScreens(Admin admin) {
        while (true) {
            try {
                List<Room> rooms = adminService.getRooms(admin.getCinemaId());

                System.out.println(
                        "\n[" + admin.getCinemaName() + "] 상영관 목록");

                if (rooms.isEmpty()) {
                    System.out.println("등록된 상영관이 없습니다.");
                }

                for (Room room : rooms) {
                    System.out.printf("%d | %s | %d층 | %s%n",
                            room.getRoomId(),
                            room.getRoomName(),
                            room.getFloor(),
                            Boolean.TRUE.equals(room.getActive())
                                    ? "운영" : "운영 불가");
                }

                String menu = readLine(
                        "[1. 상태 수정] [2. 뒤로가기] [0. 종료하기]: ").trim();

                // 관리자 메뉴로 돌아가기
                if ("2".equals(menu)) {
                    return;
                }

                // 프로그램 전체 종료
                if ("0".equals(menu)) {
                    String confirm = readLine(
                            "프로그램을 종료하시겠습니까? (Y/N): ").trim();

                    if ("Y".equalsIgnoreCase(confirm)) {
                        System.out.println("프로그램을 종료합니다.");
                        System.exit(0);
                    }

                    continue;
                }

                if (!"1".equals(menu)) {
                    System.out.println("올바른 번호를 입력하세요.");
                    continue;
                }

                if (rooms.isEmpty()) {
                    System.out.println("수정할 상영관이 없습니다.");
                    continue;
                }

                int roomId = Integer.parseInt(
                        readLine("수정할 관 ID (0: 취소): ").trim());

                if (roomId == 0) {
                    continue;
                }

                if (rooms.stream().noneMatch(
                        room -> room.getRoomId().equals(roomId))) {
                    System.out.println("목록에 있는 관 ID를 입력하세요.");
                    continue;
                }

                String choice = readLine(
                        "[1. 운영 가능] [2. 운영 불가] [0. 취소]: ").trim();

                if ("0".equals(choice)) {
                    continue;
                }

                if (!"1".equals(choice) && !"2".equals(choice)) {
                    System.out.println("올바른 번호를 입력하세요.");
                    continue;
                }

                String confirm = readLine(
                        "변경하시겠습니까? (Y/N): ").trim();

                if (!"Y".equalsIgnoreCase(confirm)) {
                    continue;
                }

                AdminService.RoomResult result = adminService.changeRoom(
                        admin.getCinemaId(), roomId, "1".equals(choice));

                System.out.println(result.message());

                for (Room buyer : result.buyers()) {
                    System.out.printf(
                            "아이디: %s | 전화번호: %s | 예매 좌석: %d개%n좌석: %s%n",
                            buyer.getLoginId(),
                            buyer.getPhoneNumber(),
                            buyer.getSeatCount(),
                            buyer.getSeatLocations() == null
                                    ? "좌석 정보 확인 필요"
                                    : buyer.getSeatLocations());
                }

            } catch (NumberFormatException e) {
                System.out.println("관 ID는 숫자로 입력하세요.");
            } catch (RuntimeException e) {
                System.out.println(
                        "처리에 실패했습니다. DB 연결과 설정을 확인하세요.");
                return;
            }
        }
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