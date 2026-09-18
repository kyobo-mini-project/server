package com.kyobo.server.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.entity.Seat;
import com.kyobo.server.mapper.ScreeningMapper;
import com.kyobo.server.mapper.SeatMapper;

public class ScreeningService {
    private static final char BOOKED = '■';
    private static final char AVAILABLE = '□';

    /**
     * 관리자용 상영회차 상세.
     * 영화·상영관 정보와 좌석 배치(예매 ■ / 미예매 □)를 채운다.
     */
    public Screening findScreeningDetail(Integer screeningId) {
        if (screeningId == null) {
            throw new IllegalArgumentException("상영회차 번호를 입력해 주세요.");
        }

        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            ScreeningMapper screeningMapper = session.getMapper(ScreeningMapper.class);
            SeatMapper seatMapper = session.getMapper(SeatMapper.class);

            Screening screening = screeningMapper.findDetailByScreeningId(screeningId);
            if (screening == null) {
                throw new IllegalArgumentException("상영 회차가 존재하지 않습니다.");
            }

            List<Seat> seats = seatMapper.findActiveByRoomId(screening.getRoomId());
            Set<Integer> bookedSeatIds = new HashSet<>(
                    seatMapper.findBookedSeatIdsByScreeningId(screeningId));

            for (Seat seat : seats) {
                seat.setBooked(bookedSeatIds.contains(seat.getSeatId()));
            }

            screening.setSeats(seats);
            screening.setSeatMap(buildSeatMap(seats));
            return screening;
        }
    }

    /** 예: {@code A □ ■ □\nB ■ □ □} */
    private static String buildSeatMap(List<Seat> seats) {
        if (seats == null || seats.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String currentRow = null;
        for (Seat seat : seats) {
            String row = seat.getRowName();
            if (!row.equals(currentRow)) {
                if (currentRow != null) {
                    sb.append('\n');
                }
                currentRow = row;
                sb.append(row).append(' ');
            } else {
                sb.append(' ');
            }
            sb.append(seat.isBooked() ? BOOKED : AVAILABLE);
        }
        return sb.toString();
    }
}
