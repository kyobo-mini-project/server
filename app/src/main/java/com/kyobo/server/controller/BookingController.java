package com.kyobo.server.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.common.GoHomeSignal;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.entity.Seat;
import com.kyobo.server.entity.User;
import com.kyobo.server.service.BookingService;

/**
 * 영화 상세조회에서 영화가 정해진 상태로 호출되는 예매 화면.
 * 로그인된 사용자만 진입한다는 전제이므로, 비로그인 체크는 호출하는 쪽(상세조회) 책임이다.
 *
 * 흐름: 상영회차 선택 -> 좌석 수 입력 -> 좌석 선택 -> 예매확인서.
 * 각 단계에서 0을 입력하면 확인 없이 바로 이전 단계로 돌아간다(상영회차 선택 단계에서는 예매 자체를 취소하고 호출한 쪽으로 복귀).
 * 좌석 선점 경쟁으로 예매가 실패해도 앱을 끝내지 않고 좌석 선택 단계로 되돌린다.
 */
public class BookingController {
    private static final String DIVIDER = "===========================================================";

    private final Scanner scanner;
    private final BookingService bookingService;

    public BookingController(Scanner scanner) {
        this.scanner = scanner;
        this.bookingService = new BookingService();
    }

    public void runBooking(int movieId, String movieTitle, User user) {
        System.out.println(DIVIDER);
        System.out.println(movieTitle);
        System.out.println("-----------------------------------------------------------");

        ApiResponse<List<Screening>> screeningsResponse = bookingService.findScreeningsByMovie(movieId);
        if (!screeningsResponse.isSuccess()) {
            System.out.println(screeningsResponse.getStatusMessage());
            return;
        }
        List<Screening> screenings = screeningsResponse.getData();
        if (screenings.isEmpty()) {
            System.out.println("현재 예매 가능한 상영회차가 없습니다.");
            return;
        }

        screeningLoop:
        while (true) {
            Screening screening = chooseScreening(screenings);
            if (screening == null) {
                return;
            }

            seatCountLoop:
            while (true) {
                int seatCount = readSeatCount();
                if (seatCount == -1) {
                    continue screeningLoop;
                }

                while (true) {
                    List<Seat> selectedSeats = chooseSeats(screening, seatCount);
                    if (selectedSeats == null) {
                        continue seatCountLoop;
                    }

                    ApiResponse<Void> bookingResponse = bookingService.createBooking(
                            user.getUserId(), movieId, screening.getScreeningId(), selectedSeats);
                    if (!bookingResponse.isSuccess()) {
                        System.out.println(bookingResponse.getStatusMessage());
                        continue;
                    }

                    printBookingComplete(screening, movieTitle);
                    return;
                }
            }
        }
    }

    /** @return 선택한 상영회차, 0 입력 시 null (예매 취소) */
    private Screening chooseScreening(List<Screening> screenings) {
        while (true) {
            System.out.println();
            System.out.println("상영회차를 선택하세요. (0: 취소)");
            for (int i = 0; i < screenings.size(); i++) {
                Screening s = screenings.get(i);
                System.out.println((i + 1) + ". " + s.getScreeningDate() + " " + s.getStartTime()
                        + " (" + s.getRoomName() + ", " + s.getCinemaName() + ")");
            }
            String input = readLine("선택: ");
            if (input.equals("0")) {
                return null;
            }
            int choice = parseIntOrDefault(input, -1);
            if (choice >= 1 && choice <= screenings.size()) {
                return screenings.get(choice - 1);
            }
            System.out.println("올바른 번호를 입력하세요.");
        }
    }

    /** @return 예매할 좌석 수, 0 입력 시 -1 (이전 단계로) */
    private int readSeatCount() {
        while (true) {
            String input = readLine("예매할 좌석 수를 입력하세요. (0: 이전 단계로): ");
            if (input.equals("0")) {
                return -1;
            }
            int count = parseIntOrDefault(input, -1);
            if (count >= 1) {
                return count;
            }
            System.out.println("1 이상의 숫자를 입력하세요.");
        }
    }

    /**
     * @return 선택한 좌석 목록, 0 입력 시 null (이전 단계로).
     *         좌석맵 조회 자체가 실패해도(DB 오류) 같은 방식으로 null을 반환해 이전 단계로 돌린다.
     */
    private List<Seat> chooseSeats(Screening screening, int seatCount) {
        ApiResponse<List<Seat>> seatMapResponse =
                bookingService.findSeatMap(screening.getRoomId(), screening.getScreeningId());
        if (!seatMapResponse.isSuccess()) {
            System.out.println(seatMapResponse.getStatusMessage());
            return null;
        }
        List<Seat> seatMap = seatMapResponse.getData();

        while (true) {
            printSeatMap(seatMap);

            String input = readLine("예매할 좌석 코드를 콤마로 구분해 입력하세요. (예: C3,C4,C5) (0: 이전 단계로): ");
            if (input.equals("0")) {
                return null;
            }

            String[] codes = input.split(",");
            if (codes.length != seatCount) {
                System.out.println("입력한 좌석 수가 " + seatCount + "개와 일치하지 않습니다.");
                continue;
            }

            List<Seat> selected = new ArrayList<>();
            Set<String> seenCodes = new LinkedHashSet<>();
            boolean allValid = true;
            for (String rawCode : codes) {
                String code = rawCode.trim().toUpperCase();
                Seat seat = findSeatByCode(seatMap, code);
                if (seat == null || seat.booked() || !seenCodes.add(code)) {
                    System.out.println(code + " : 예매가 불가능한 좌석입니다.");
                    allValid = false;
                } else {
                    selected.add(seat);
                }
            }
            if (allValid) {
                return selected;
            }
        }
    }

    private Seat findSeatByCode(List<Seat> seatMap, String code) {
        for (Seat seat : seatMap) {
            if (seat.seatCode().equalsIgnoreCase(code)) {
                return seat;
            }
        }
        return null;
    }

    private void printSeatMap(List<Seat> seatMap) {
        System.out.println();
        System.out.println("□는 예약가능좌석입니다 / ■는 예약불가 좌석입니다");

        Map<String, Map<Integer, Seat>> grid = new TreeMap<>();
        Set<Integer> colNums = new TreeSet<>();
        for (Seat seat : seatMap) {
            grid.computeIfAbsent(seat.getRowName(), k -> new TreeMap<>()).put(seat.getColNum(), seat);
            colNums.add(seat.getColNum());
        }

        StringBuilder header = new StringBuilder("   ");
        for (int col : colNums) {
            header.append(String.format("%-3d", col));
        }
        System.out.println(header);

        for (Map.Entry<String, Map<Integer, Seat>> rowEntry : grid.entrySet()) {
            StringBuilder line = new StringBuilder(rowEntry.getKey() + "  ");
            for (int col : colNums) {
                Seat seat = rowEntry.getValue().get(col);
                if (seat == null) {
                    line.append("   ");
                } else {
                    line.append(seat.booked() ? "■  " : "□  ");
                }
            }
            System.out.println(line);
        }
        System.out.println();
    }

    /** 예매 완료 후에는 되돌아갈 단계가 없으므로, 0 입력 시 상세조회를 건너뛰고 로그인 후 메인 메뉴로 바로 돌아간다. */
    private void printBookingComplete(Screening screening, String movieTitle) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("★ 예매확인서 ★");
        System.out.println("-----------------------------------------------------------");
        System.out.println(screening.getCinemaName());
        System.out.println(movieTitle);
        System.out.println(screening.getRoomName());
        System.out.println(screening.getFloor() + "층");
        System.out.println(screening.getScreeningDate() + " " + screening.getStartTime());
        System.out.println(DIVIDER);

        String input = readLine("0을 입력하면 홈으로 돌아갑니다: ");
        if (input.equals("0")) {
            throw new GoHomeSignal();
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String readLine(String prompt) {
        if (!prompt.isBlank()) {
            System.out.print(prompt);
        }
        return scanner.nextLine();
    }
}
