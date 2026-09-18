package com.kyobo.server.service;

import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.BookedSeat;
import com.kyobo.server.entity.Booking;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.entity.Seat;
import com.kyobo.server.mapper.BookedSeatMapper;
import com.kyobo.server.mapper.BookingMapper;
import com.kyobo.server.mapper.ScreeningMapper;
import com.kyobo.server.mapper.SeatMapper;

public class BookingService {
    public ApiResponse<List<Screening>> findScreeningsByMovie(int movieId) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            ScreeningMapper mapper = session.getMapper(ScreeningMapper.class);
            return ApiResponse.success(mapper.findByMovieId(movieId));
        } catch (Exception e) {
            return ApiResponse.error("상영회차 조회 중 오류가 발생했습니다.");
        }
    }

    public ApiResponse<List<Seat>> findSeatMap(int roomId, int screeningId) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            SeatMapper mapper = session.getMapper(SeatMapper.class);
            return ApiResponse.success(mapper.findSeatMap(roomId, screeningId));
        } catch (Exception e) {
            return ApiResponse.error("좌석 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 예매 1건 + 선택 좌석 N건을 한 트랜잭션으로 저장한다.
     * 이미 다른 예매가 선점한 좌석이면(booked_seats UNIQUE 제약 위반) 커밋하지 않고 실패로 응답한다.
     */
    public ApiResponse<Void> createBooking(int userId, int movieId, int screeningId, List<Seat> selectedSeats) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            BookingMapper bookingMapper = session.getMapper(BookingMapper.class);
            BookedSeatMapper bookedSeatMapper = session.getMapper(BookedSeatMapper.class);

            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setMovieId(String.valueOf(movieId));
            booking.setScreeningId(screeningId);
            bookingMapper.insert(booking);

            for (Seat seat : selectedSeats) {
                BookedSeat bookedSeat = new BookedSeat();
                bookedSeat.setBookingId(booking.getBookingId());
                bookedSeat.setSeatId(seat.getSeatId());
                bookedSeat.setScreeningId(screeningId);
                bookedSeatMapper.insert(bookedSeat);
            }

            session.commit();
            return ApiResponse.success(null);
        } catch (PersistenceException e) {
            return ApiResponse.fail("03", "이미 다른 예매로 선점된 좌석이 있어 예매에 실패했습니다. 다시 선택해 주세요.");
        } catch (Exception e) {
            return ApiResponse.error("예매 처리 중 오류가 발생했습니다.");
        }
    }
}
