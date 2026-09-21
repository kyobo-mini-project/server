package com.kyobo.server.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.common.GoHomeSignal;
import com.kyobo.server.entity.Booking;
import com.kyobo.server.entity.Cinema;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.entity.Seat;
import com.kyobo.server.entity.User;
import com.kyobo.server.service.BookingService;

/**
 * 영화 상세조회에서 영화가 정해진 상태로 호출되는 예매 화면.
 * 로그인된 사용자만 진입한다는 전제이므로, 비로그인 체크는 호출하는 쪽(상세조회) 책임이다.
 *
 * 흐름: 영화관 선택 -> 날짜 선택 -> 상영회차 선택 -> 좌석 수 입력 -> 좌석 선택 -> 예매확인서.
 * (한 영화가 여러 영화관·한 달 가까이 상영되므로, 영화관과 날짜를 먼저 좁혀야 상영회차 목록이 감당할 만한 길이가 된다.)
 * 각 단계에서 0을 입력하면 확인 없이 바로 이전 단계로 돌아간다(영화관 선택 단계에서는 예매 자체를 취소하고 호출한 쪽으로 복귀).
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

        ApiResponse<List<Cinema>> cinemasResponse = bookingService.findCinemasByMovie(movieId);
        if (!cinemasResponse.isSuccess()) {
            System.out.println(cinemasResponse.getStatusMessage());
            return;
        }
        List<Cinema> cinemas = cinemasResponse.getData();
        if (cinemas.isEmpty()) {
            System.out.println("현재 예매 가능한 영화관이 없습니다.");
            return;
        }

        cinemaLoop:
        while (true) {
            Cinema cinema = chooseCinema(cinemas);
            if (cinema == null) {
                return;
            }

            ApiResponse<List<Screening>> screeningsResponse =
                    bookingService.findScreenings(movieId, cinema.getCinemaId());
            if (!screeningsResponse.isSuccess()) {
                System.out.println(screeningsResponse.getStatusMessage());
                continue;
            }
            List<Screening> screenings = screeningsResponse.getData();
            if (screenings.isEmpty()) {
                System.out.println("해당 영화관에는 예매 가능한 상영회차가 없습니다.");
                continue;
            }

            dateLoop:
            while (true) {
                LocalDate date = chooseDate(screenings);
                if (date == null) {
                    continue cinemaLoop;
                }

                List<Screening> screeningsOnDate = screenings.stream()
                        .filter(s -> s.getScreeningDate().equals(date))
                        .collect(Collectors.toList());
                if (screeningsOnDate.isEmpty()) {
                    System.out.println("해당 날짜에는 상영회차가 없습니다.");
                    continue;
                }

                screeningLoop:
                while (true) {
                    Screening screening = chooseScreening(screeningsOnDate);
                    if (screening == null) {
                        continue dateLoop;
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

                            printBookingComplete(screening, movieTitle, selectedSeats);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void runHistory(User user) {
        ApiResponse<List<Booking>> historyResponse = bookingService.findBookingHistory(user.getUserId());
        if (!historyResponse.isSuccess()) {
            System.out.println(historyResponse.getStatusMessage());
            return;
        }
        List<Booking> bookings = historyResponse.getData();
        if (bookings.isEmpty()) {
            System.out.println("예매 내역이 없습니다.");
            return;
        }

        while (true) {
            Booking selected = chooseFromList(bookings, "예매 내역을 선택하세요. (0: 이전 화면으로)",
                    b -> b.getMovieTitle() + " / " + b.getCinemaName() + " / " + b.getScreeningDate() + " "
                            + b.getStartTime() + " / 좌석: " + b.getSeatCodes());
            if (selected == null) {
                return;
            }
            printBookingDetail(selected);
        }
    }

    private void printBookingDetail(Booking booking) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("예매 번호: " + booking.getBookingId());
        System.out.println("-----------------------------------------------------------");
        System.out.println(booking.getMovieTitle());
        System.out.println(booking.getCinemaName() + " " + booking.getFloor() + "층 " + booking.getRoomName());
        System.out.println(booking.getSeatCodes());
        System.out.println(booking.getScreeningDate() + " " + booking.getStartTime());
        System.out.println("예매 상태: " + statusLabel(booking.getBookingStatus()));
        System.out.println(DIVIDER);

        String input = readLine("1: 예매 취소, 0: 목록으로 돌아가기: ");
        if (input.equals("1")) {
            // TODO: 예매 취소 기능 구현 예정
            System.out.println("예매 취소 기능은 준비 중입니다.");
        }
    }

    private String statusLabel(String status) {
        return "CANCELED".equals(status) ? "예매 취소" : "예매완료";
    }

    /** @return 선택한 날짜, 0 입력 시 null (이전 단계로) */
    private LocalDate chooseDate(List<Screening> screenings) {
        LocalDate minDate = screenings.stream().map(Screening::getScreeningDate).min(LocalDate::compareTo).orElse(null);
        LocalDate maxDate = screenings.stream().map(Screening::getScreeningDate).max(LocalDate::compareTo).orElse(null);

        while (true) {
            System.out.println();
            System.out.println("관람 날짜를 입력하세요. (관람 가능 기간: " + minDate + " ~ " + maxDate + ") (0: 이전 단계로)");
            String input = readLine("날짜 (예: 2026-09-15): ");
            if (input.equals("0")) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("날짜 형식이 올바르지 않습니다. (예: 2026-09-15)");
            }
        }
    }

    /** @return 선택한 영화관, 0 입력 시 null (예매 취소) */
    private Cinema chooseCinema(List<Cinema> cinemas) {
        return chooseFromList(cinemas, "영화관을 선택하세요. (0: 취소)", Cinema::getCinemaName);
    }

    /** @return 선택한 상영회차, 0 입력 시 null (이전 단계로) */
    private Screening chooseScreening(List<Screening> screenings) {
        String header = screenings.get(0).getScreeningDate() + " 상영회차를 선택하세요. (0: 이전 단계로)";
        return chooseFromList(screenings, header,
                s -> s.getStartTime() + "~" + s.getEndTime() + " (" + s.getRoomName() + ")");
    }

    /** 번호 매긴 목록을 보여주고 하나를 고르게 한다. @return 고른 항목, 0 입력 시 null */
    private <T> T chooseFromList(List<T> items, String header, Function<T, String> labelFn) {
        while (true) {
            System.out.println();
            System.out.println(header);
            for (int i = 0; i < items.size(); i++) {
                System.out.println((i + 1) + ". " + labelFn.apply(items.get(i)));
            }
            String input = readLine("선택: ");
            if (input.equals("0")) {
                return null;
            }
            int choice = parseIntOrDefault(input, -1);
            if (choice >= 1 && choice <= items.size()) {
                return items.get(choice - 1);
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

        // 좌석 칸(기호 1글자 + 공백 2칸 = 3칸)과 너비를 맞춘다.
        int gridWidth = 3 + colNums.size() * 3;
        printScreenIndicator(gridWidth);

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

    /** 좌석 그리드 너비에 맞춰 가운데 정렬된 스크린 표시줄을 출력한다. */
    private void printScreenIndicator(int gridWidth) {
        String label = " 스크린 ";
        int dashCount = Math.max(gridWidth - label.length(), 4);
        int leftDashes = dashCount / 2;
        int rightDashes = dashCount - leftDashes;
        System.out.println("-".repeat(leftDashes) + label + "-".repeat(rightDashes));
    }

    /** 예매 완료 후에는 되돌아갈 단계가 없으므로, 0 입력 시 상세조회를 건너뛰고 로그인 후 메인 메뉴로 바로 돌아간다. */
    private void printBookingComplete(Screening screening, String movieTitle, List<Seat> selectedSeats) {
        String seatCodes = selectedSeats.stream().map(Seat::seatCode).collect(Collectors.joining(", "));

        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("★ 예매확인서 ★");
        System.out.println("-----------------------------------------------------------");
        System.out.println(movieTitle);
        System.out.println(screening.getCinemaName());
        System.out.println(screening.getFloor() + "층");
        System.out.println(screening.getRoomName());
        System.out.println(seatCodes);
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
        return scanner.nextLine().trim();
    }
}
