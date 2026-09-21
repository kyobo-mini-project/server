package com.kyobo.server.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.Movie;
import com.kyobo.server.entity.Room;
import com.kyobo.server.entity.Screening;
import com.kyobo.server.mapper.MovieMapper;
import com.kyobo.server.mapper.RoomMapper;
import com.kyobo.server.mapper.ScreeningMapper;

public class ScreeningService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

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

    public List<Movie> findAllMovies() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(MovieMapper.class).findAll();
        }
    }

    public Movie findMovieById(int movieId) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(MovieMapper.class).findById(movieId);
        }
    }

    /**
     * 상영회차를 등록한다.
     * endTime = startTime + runningTime(분)
     * 같은 관의 시간 겹침이 있으면 실패한다.
     */
    public Screening createScreening(
            int cinemaId,
            int roomId,
            int movieId,
            LocalDate date,
            LocalTime startTime
    ) {
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해 주세요.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("시작 시간을 입력해 주세요.");
        }
        if (startTime.isBefore(LocalTime.of(6, 0))) {
            throw new IllegalArgumentException("상영 시작 시간은 조조 기준 06:00부터 입력할 수 있습니다.");
        }
        if (!LocalDateTime.of(date, startTime).isAfter(LocalDateTime.now(SEOUL))) {
            throw new IllegalArgumentException("이미 지난 날짜와 시간에는 상영회차를 등록할 수 없습니다.");
        }

        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            MovieMapper movieMapper = session.getMapper(MovieMapper.class);
            RoomMapper roomMapper = session.getMapper(RoomMapper.class);
            ScreeningMapper screeningMapper = session.getMapper(ScreeningMapper.class);

            Movie movie = movieMapper.findById(movieId);
            if (movie == null) {
                throw new IllegalArgumentException("존재하지 않는 영화입니다.");
            }
            if (movie.getRunningTime() == null || movie.getRunningTime() <= 0) {
                throw new IllegalArgumentException("영화 상영시간이 등록되어 있지 않습니다.");
            }

            Room selectedRoom = null;
            for (Room room : roomMapper.findActiveByCinemaId(cinemaId)) {
                if (room.getRoomId() == roomId) {
                    selectedRoom = room;
                    break;
                }
            }
            if (selectedRoom == null) {
                throw new IllegalArgumentException("해당 지점에서 사용할 수 없는 상영관입니다.");
            }

            LocalTime endTime = startTime.plusMinutes(movie.getRunningTime());

            int overlaps = screeningMapper.countOverlaps(roomId, date, startTime, endTime);
            if (overlaps > 0) {
                throw new IllegalStateException("선택한 상영관에 이미 겹치는 상영회차가 있습니다.");
            }

            Screening screening = new Screening();
            screening.setCinemaId(cinemaId);
            screening.setRoomId(roomId);
            screening.setMovieId(movieId);
            screening.setScreeningDate(date);
            screening.setStartTime(startTime);
            screening.setEndTime(endTime);
            screening.setMovieTitle(movie.getTitle());
            screening.setRoomName(selectedRoom.getRoomName());

            screeningMapper.insert(screening);
            session.commit();
            return screening;
        }
    }
}
