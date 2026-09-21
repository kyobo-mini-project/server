package com.kyobo.server.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.entity.Admin;
import com.kyobo.server.entity.Movie;
import com.kyobo.server.entity.Room;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.service.AdminService;
import com.kyobo.server.service.ScreeningService;

public class AdminController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Scanner scanner;
    private final AdminService adminService;
    private final ScreeningService screeningService;

    public AdminController(Scanner scanner) {
        this.scanner = scanner;
        this.adminService = new AdminService();
        this.screeningService = new ScreeningService();
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
                    manageShowtimes(admin);
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

    private void manageShowtimes(Admin admin) {
        while (true) {
            System.out.println();
            System.out.println("===== 상영회차 관리 =====");
            System.out.println("[1. 상영회차 조회] [2. 상영회차 등록] [0. 뒤로가기]");
            String choice = readLine("선택: ");
            switch (choice) {
                case "1":
                    listScreenings(admin);
                    break;
                case "2":
                    createScreening(admin);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("올바른 번호를 입력하세요.");
            }
        }
    }

    private void listScreenings(Admin admin) {
        LocalDate date = readDate("조회할 날짜를 입력하세요. (예: 2026-09-22, 0: 취소): ");
        if (date == null) {
            return;
        }

        try {
            List<Room> rooms = screeningService.findActiveRooms(admin.getCinemaId());
            List<Screening> screenings = screeningService.findByCinemaAndDate(admin.getCinemaId(), date);
            printScreeningsByRoom(admin.getCinemaName(), date, rooms, screenings);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createScreening(Admin admin) {
        LocalDate date = readDate("등록할 날짜를 입력하세요. (예: 2026-09-22, 0: 취소): ");
        if (date == null) {
            return;
        }

        List<Room> rooms = screeningService.findActiveRooms(admin.getCinemaId());
        if (rooms.isEmpty()) {
            System.out.println("등록 가능한 상영관이 없습니다.");
            return;
        }

        List<Screening> existing = screeningService.findByCinemaAndDate(admin.getCinemaId(), date);
        printScreeningsByRoom(admin.getCinemaName(), date, rooms, existing);
        System.out.println();
        System.out.println("위 시간표를 참고해 상영회차를 등록합니다.");

        Movie movie = chooseMovie();
        if (movie == null) {
            return;
        }

        Room room = chooseRoom(rooms);
        if (room == null) {
            return;
        }

        while (true) {
            LocalTime startTime = readTime("시작 시간 (예: 14:30, 0: 취소): ");
            if (startTime == null) {
                return;
            }

            try {
                Screening created = screeningService.createScreening(
                        admin.getCinemaId(),
                        room.getRoomId(),
                        movie.getMovieId(),
                        date,
                        startTime
                );
                System.out.println("상영회차가 등록되었습니다!");
                System.out.println(
                        created.getScreeningId()
                                + " | "
                                + formatTime(created.getStartTime())
                                + "-"
                                + formatTime(created.getEndTime())
                                + " | "
                                + displayText(created.getMovieTitle())
                                + " | "
                                + displayText(created.getRoomName()).trim()
                );
                return;
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                System.out.println("시작 시간을 다시 입력해 주세요.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.println("시작 시간을 다시 입력해 주세요.");
            }
        }
    }

    private Movie chooseMovie() {
        List<Movie> movies = screeningService.findAllMovies();
        if (movies.isEmpty()) {
            System.out.println("등록된 영화가 없습니다.");
            return null;
        }

        System.out.println();
        System.out.println("===== 영화 목록 =====");
        for (Movie movie : movies) {
            String running = movie.getRunningTime() == null ? "미등록" : movie.getRunningTime() + "분";
            System.out.println(movie.getMovieId() + " | " + displayText(movie.getTitle()) + " | " + running);
        }

        while (true) {
            String input = readLine("영화 번호 (0: 취소): ");
            if ("0".equals(input.trim())) {
                return null;
            }
            Integer movieId = parsePositiveInt(input);
            if (movieId == null) {
                System.out.println("영화 번호를 숫자로 입력해 주세요.");
                continue;
            }
            Movie movie = screeningService.findMovieById(movieId);
            if (movie == null) {
                System.out.println("존재하지 않는 영화입니다.");
                continue;
            }
            return movie;
        }
    }

    private Room chooseRoom(List<Room> rooms) {
        System.out.println();
        System.out.println("===== 상영관 목록 =====");
        for (Room room : rooms) {
            System.out.println(room.getRoomId() + " | " + displayText(room.getRoomName()).trim());
        }

        while (true) {
            String input = readLine("상영관 번호 (0: 취소): ");
            if ("0".equals(input.trim())) {
                return null;
            }
            Integer roomId = parsePositiveInt(input);
            if (roomId == null) {
                System.out.println("상영관 번호를 숫자로 입력해 주세요.");
                continue;
            }
            for (Room room : rooms) {
                if (room.getRoomId().equals(roomId)) {
                    return room;
                }
            }
            System.out.println("목록에 있는 상영관 번호만 입력해 주세요.");
        }
    }

    private LocalTime readTime(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if ("0".equals(input.trim())) {
                return null;
            }
            try {
                return LocalTime.parse(input.trim(), TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("시간 형식이 올바르지 않습니다. (예: 14:30)");
            }
        }
    }

    private Integer parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void printScreeningsByRoom(
            String cinemaName,
            LocalDate date,
            List<Room> rooms,
            List<Screening> screenings
    ) {
        System.out.println();
        System.out.println("===== [" + cinemaName + "] " + date + " 상영회차 =====");

        if (rooms == null || rooms.isEmpty()) {
            System.out.println("상영 없음");
            return;
        }

        Map<Integer, List<Screening>> byRoom = groupByRoom(screenings);
        for (Room room : rooms) {
            System.out.println("[" + displayText(room.getRoomName()).trim() + "]");
            List<Screening> roomScreenings = byRoom.getOrDefault(room.getRoomId(), List.of());
            if (roomScreenings.isEmpty()) {
                System.out.println("상영 없음");
                continue;
            }
            for (Screening screening : roomScreenings) {
                System.out.println(
                        screening.getScreeningId()
                                + " | "
                                + formatTime(screening.getStartTime())
                                + "-"
                                + formatTime(screening.getEndTime())
                                + " | "
                                + displayText(screening.getMovieTitle())
                );
            }
        }
    }

    private Map<Integer, List<Screening>> groupByRoom(List<Screening> screenings) {
        Map<Integer, List<Screening>> byRoom = new LinkedHashMap<>();
        if (screenings == null) {
            return byRoom;
        }
        for (Screening screening : screenings) {
            byRoom.computeIfAbsent(screening.getRoomId(), key -> new ArrayList<>()).add(screening);
        }
        return byRoom;
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if ("0".equals(input.trim())) {
                return null;
            }
            try {
                return LocalDate.parse(input.trim());
            } catch (DateTimeParseException e) {
                System.out.println("날짜 형식이 올바르지 않습니다. (예: 2026-09-22)");
            }
        }
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return "--:--";
        }
        return time.format(TIME_FORMAT);
    }

    private String displayText(String value) {
        if (value == null || value.isBlank()) {
            return "미등록";
        }
        return value;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
