package com.kyobo.server.service;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.Room;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.mapper.RoomMapper;
import com.kyobo.server.mapper.ScreeningMapper;

public class ScreeningService {

    public List<Room> findActiveRooms(int cinemaId) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(RoomMapper.class).findActiveByCinemaId(cinemaId);
        }
    }

    public List<Screening> findByCinemaAndDate(int cinemaId, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해 주세요.");
        }
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(ScreeningMapper.class).findByCinemaAndDate(cinemaId, date);
        }
    }
}
