package com.kyobo.server.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kyobo.server.entity.Cinema;
import com.kyobo.server.entity.Screening;

public interface ScreeningMapper {
    List<Cinema> findCinemasByMovie(int movieId);

    List<Screening> findByMovieAndCinema(@Param("movieId") int movieId, @Param("cinemaId") int cinemaId);

    List<Screening> findByCinemaAndDate(@Param("cinemaId") int cinemaId, @Param("date") LocalDate date);
}
