package com.kyobo.server.service;

/**
 * 점유율(%) = 유효 좌석(티켓) 수 ÷ 해당 영화의 전체 판매 가능 좌석 수 × 100
 */
public final class OccupancyCalculator {
    private OccupancyCalculator() {
    }

    /**
     * @param soldSeats      booking_status = DONE 인 예매 좌석 수
     * @param availableSeats 해당 영화 상영회차의 판매 가능 좌석 수
     * @return 점유율(%). 판매 가능 좌석이 없으면 0.0
     */
    public static double calculate(long soldSeats, long availableSeats) {
        if (availableSeats <= 0) {
            return 0.0;
        }
        if (soldSeats < 0) {
            soldSeats = 0;
        }
        return soldSeats * 100.0 / availableSeats;
    }
}
