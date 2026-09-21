package com.kyobo.server.service;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.config.FieldEncryptor;
import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.Admin;
import com.kyobo.server.mapper.AdminMapper;
import java.util.List;
import com.kyobo.server.entity.RoomAdmin;

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

    public record RoomResult(String message, List<RoomAdmin> buyers) {}

    public List<RoomAdmin> getRooms(int cinemaId) {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(AdminMapper.class)
                    .findRoomsByCinemaId(cinemaId);
        }
    }

    public RoomResult changeRoom(int cinemaId, int roomId, boolean active) {
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
                    return new RoomResult("해당 지점의 상영관이 아닙니다.", List.of());
                }

                if (Boolean.valueOf(active).equals(room.getActive())) {
                    session.rollback();
                    return new RoomResult("이미 같은 상태입니다.", List.of());
                }

                if (!active) {
                    List<RoomAdmin> buyers = mapper.findBookedBuyers(cinemaId, roomId);

                    if (!buyers.isEmpty()) {
                        session.rollback();

                        for (RoomAdmin buyer : buyers) {
                            try {
                                buyer.setPhoneNumber(
                                        FieldEncryptor.decrypt(buyer.getPhoneNumber()));
                            } catch (RuntimeException e) {
                                buyer.setPhoneNumber("복호화 실패");
                            }
                        }

                        return new RoomResult(
                                "예매자가 있어 운영 불가로 변경할 수 없습니다.", buyers);
                    }
                }

                if (mapper.updateRoomActive(cinemaId, roomId, active) != 1) {
                    throw new IllegalStateException("상태 변경 실패");
                }

                session.commit();
                return new RoomResult("상태를 변경했습니다.", List.of());

            } catch (RuntimeException e) {
                session.rollback();
                throw e;
            }
        }
    }
}
