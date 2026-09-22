package com.kyobo.server.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
import com.kyobo.server.entity.RoomAdmin;
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
                    manageScreens(admin);
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
        MovieController movieController = new MovieController(scanner);

        while (true) {
            System.out.println("\n===== 영화 관리 =====");
            System.out.println("[1 영화 등록] [2 영화 삭제] [0 뒤로가기]");

            String choice = readLine("선택: ").trim();

            switch (choice) {
                case "1":
                    movieController.runMovieRegistration();
                    break;

                case "2":
                    movieController.runMovieDeletion();
                    break;

                case "0":
                    return;

                default:
                    System.out.println("올바른 번호를 입력하세요.");
            }
        }
    }

    private void manageScreens(Admin admin) {
        while (true) {
            try {
                List<RoomAdmin> rooms = adminService.getRooms(admin.getCinemaId());

                System.out.println(
                        "\n[" + admin.getCinemaName() + "] 상영관 목록");

                if (rooms.isEmpty()) {
                    System.out.println("등록된 상영관이 없습니다.");
                }

                for (RoomAdmin room : rooms) {
                    System.out.printf("%d | %s | %d층 | %s%n",
                            room.getRoomId(),
                            room.getRoomName(),
                            room.getFloor(),
                            Boolean.TRUE.equals(room.getActive())
                                    ? "운영" : "운영 불가");
                }

                String menu = readLine(
                        "[1. 상태 수정] [0. 뒤로가기] : ").trim();

                // 관리자 메뉴로 돌아가기
                if ("0".equals(menu)) {
                    return;
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


                Map<Integer, List<RoomAdmin>> seatsByBooking = new LinkedHashMap<>();

                for (RoomAdmin seat : result.seats()) {
                    seatsByBooking
                            .computeIfAbsent(seat.getBookingId(), key -> new ArrayList<>())
                            .add(seat);
                }


                Map<Integer, List<RoomAdmin>> bookingsByUser = new LinkedHashMap<>();

                for (RoomAdmin booking : result.buyers()) {
                    bookingsByUser
                            .computeIfAbsent(booking.getUserId(), key -> new ArrayList<>())
                            .add(booking);
                }

                for (List<RoomAdmin> bookings : bookingsByUser.values()) {
                    RoomAdmin buyer = bookings.get(0);
                    Map<Integer, List<String>> seatsByScreening = new LinkedHashMap<>();
                    int seatCount = 0;

                    for (RoomAdmin booking : bookings) {
                        List<RoomAdmin> seats = seatsByBooking.getOrDefault(
                                booking.getBookingId(), List.of());

                        List<String> labels = seatsByScreening.computeIfAbsent(
                                booking.getScreeningId(), key -> new ArrayList<>());

                        if (seats.isEmpty()) {
                            labels.add("좌석 정보 확인 필요");
                            continue;
                        }

                        for (RoomAdmin seat : seats) {
                            seatCount++;

                            if (seat.getRowName() == null || seat.getColNum() == null) {
                                labels.add("좌석 정보 확인 필요");
                            } else {
                                labels.add(seat.getRowName().trim() + seat.getColNum());
                            }
                        }
                    }

                    System.out.printf(
                            "아이디: %s | 전화번호: %s | 예매 좌석: %d개%n",
                            buyer.getLoginId(), buyer.getPhoneNumber(), seatCount);

                    System.out.println("좌석:");

                    seatsByScreening.forEach((screeningId, seats) ->
                            System.out.println(
                                    "회차 " + screeningId + ": " + String.join(", ", seats)));

                    System.out.println();
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
        LocalDate date = readRegistrationDate();
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
            LocalTime startTime = readTime("시작 시간 (정각 또는 30분, 예: 14:00, 0: 취소): ");
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
        int idWidth = 1;
        int titleWidth = 0;
        for (Movie movie : movies) {
            idWidth = Math.max(idWidth, displayWidth(String.valueOf(movie.getMovieId())));
            titleWidth = Math.max(titleWidth, displayWidth(displayText(movie.getTitle())));
        }
        for (Movie movie : movies) {
            String running = movie.getRunningTime() == null ? "미등록" : movie.getRunningTime() + "분";
            System.out.println(
                    padDisplay(String.valueOf(movie.getMovieId()), idWidth)
                            + " | "
                            + padDisplay(displayText(movie.getTitle()), titleWidth)
                            + " | "
                            + running
            );
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

    private LocalDate readRegistrationDate() {
        while (true) {
            LocalDate date = readDate("등록할 날짜를 입력하세요. (예: 2026-09-22, 0: 취소): ");
            if (date == null) {
                return null;
            }
            if (date.isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")))) {
                System.out.println("이미 지난 날짜에는 상영회차를 등록할 수 없습니다.");
                continue;
            }
            return date;
        }
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

    private String padDisplay(String text, int width) {
        int padding = width - displayWidth(text);
        if (padding <= 0) {
            return text;
        }
        return text + " ".repeat(padding);
    }

    /** 터미널 기준 표시 폭. 한글 등 전각은 2칸으로 계산한다. */
    private int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            width += isWideChar(codePoint) ? 2 : 1;
        }
        return width;
    }

    private boolean isWideChar(int codePoint) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || (codePoint >= 0xFF01 && codePoint <= 0xFF60);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}