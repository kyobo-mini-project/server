package com.kyobo.server.service;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.config.FieldEncryptor;
import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.Admin;
import com.kyobo.server.mapper.AdminMapper;
import java.util.List;
import com.kyobo.server.entity.RoomAdmin;
import com.kyobo.server.entity.Seat;
import com.kyobo.server.mapper.SeatMapper;

public class AdminService {
    public ApiResponse<Admin> findAdminByCode(String code) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            AdminMapper adminMapper = session.getMapper(AdminMapper.class);
            Admin admin = adminMapper.findByCode(code);

            if (admin == null) {
                return ApiResponse.fail("01", "존재하지 않는 관리자 코드입니다.");
            }
            return ApiResponse.success(admin);
        } catch (Exception e) {
            return ApiResponse.error("서버 오류가 발생했습니다.");
        }
    }

    public ApiResponse<Void> verifyAdminPassword(String rawPassword, String hashedPassword) {
        try {
            if (FieldEncryptor.matchesPassword(rawPassword, hashedPassword)) {
                return ApiResponse.success(null);
            }
            return ApiResponse.fail("02", "비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            return ApiResponse.error("서버 오류가 발생했습니다.");
        }
    }

    public enum RoomChangeStatus {
        SUCCESS,
        NOT_FOUND,
        SAME_STATUS,
        HAS_BOOKINGS
    }

    public record RoomChangeResult(
            RoomChangeStatus status,
            List<RoomAdmin> buyers,
            List<RoomAdmin> seats) {

        public RoomChangeResult(RoomChangeStatus status) {
            this(status, List.of(), List.of());
        }
    }

    public enum SeatChangeStatus {
        SUCCESS,
        NOT_FOUND,
        SAME_STATUS,
        HAS_BOOKINGS
    }

    public record SeatChangeResult(
            SeatChangeStatus status,
            List<RoomAdmin> bookings) {

        public SeatChangeResult(SeatChangeStatus status) {
            this(status, List.of());
        }
    }

    public List<RoomAdmin> getRooms(int cinemaId) {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            return session.getMapper(AdminMapper.class)
                    .findRoomsByCinemaId(cinemaId);
        }
    }

    public RoomChangeResult changeRoom(int cinemaId, int roomId, boolean active) {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession(false)) {
            try {
                AdminMapper mapper = session.getMapper(AdminMapper.class);

                if (!active) {
                    mapper.lockBookings();
                }

                RoomAdmin room = mapper.findRoomForUpdate(cinemaId, roomId);

                if (room == null) {
                    session.rollback();
                    return new RoomChangeResult(RoomChangeStatus.NOT_FOUND);                }

                if (Boolean.valueOf(active).equals(room.getActive())) {
                    session.rollback();
                    return new RoomChangeResult(RoomChangeStatus.SAME_STATUS);
                }

                if (!active) {
                    List<RoomAdmin> buyers = mapper.findBookedBuyers(cinemaId, roomId);

                    if (!buyers.isEmpty()) {
                        List<Integer> bookingIds = buyers.stream()
                                .map(RoomAdmin::getBookingId)
                                .distinct()
                                .toList();

                        List<RoomAdmin> seats =
                                mapper.findSeatsByBookingIds(bookingIds);

                        session.rollback();

                        for (RoomAdmin buyer : buyers) {
                            try {
                                buyer.setPhoneNumber(
                                        FieldEncryptor.decrypt(buyer.getPhoneNumber()));
                            } catch (RuntimeException e) {
                                buyer.setPhoneNumber("복호화 실패");
                            }
                        }

                        return new RoomChangeResult(
                                RoomChangeStatus.HAS_BOOKINGS,
                                buyers,
                                seats
                        );
                    }
                }

                if (mapper.updateRoomActive(cinemaId, roomId, active) != 1) {
                    throw new IllegalStateException("상태 변경 실패");
                }

                session.commit();

                return new RoomChangeResult(
                        RoomChangeStatus.SUCCESS
                );

            } catch (RuntimeException e) {
                session.rollback();
                throw e;
            }
        }
    }

    public List<Seat> getRoomSeats(int cinemaId, int roomId) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(SeatMapper.class).findByRoom(cinemaId, roomId);
        }
    }

    public SeatChangeResult changeSeat(
            int cinemaId,
            int roomId,
            int seatId,
            boolean active) {

        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession(false)) {

            try {
                SeatMapper mapper = session.getMapper(SeatMapper.class);

                Seat seat = mapper.findForUpdate(cinemaId, roomId, seatId);

                if (seat == null) {
                    session.rollback();
                    return new SeatChangeResult(
                            SeatChangeStatus.NOT_FOUND);
                }

                if (Boolean.valueOf(active).equals(seat.getActive())) {
                    session.rollback();
                    return new SeatChangeResult(
                            SeatChangeStatus.SAME_STATUS);
                }

                if (!active) {
                    List<RoomAdmin> bookings =
                            mapper.findUpcomingBookings(seatId);

                    if (!bookings.isEmpty()) {
                        session.rollback();

                        return new SeatChangeResult(
                                SeatChangeStatus.HAS_BOOKINGS,
                                bookings);
                    }
                }

                if (mapper.updateActive(seatId, active) != 1) {
                    throw new IllegalStateException(
                            "좌석 상태 변경에 실패했습니다.");
                }

                session.commit();

                return new SeatChangeResult(
                        SeatChangeStatus.SUCCESS);

            } catch (RuntimeException e) {
                session.rollback();
                throw e;
            }
        }
    }
}
