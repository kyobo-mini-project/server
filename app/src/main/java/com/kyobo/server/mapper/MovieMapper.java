package com.kyobo.server.mapper;

import java.util.List;

import com.kyobo.server.entity.Movie;
import org.apache.ibatis.annotations.Param;

import com.kyobo.server.entity.Movie;
import com.kyobo.server.entity.MovieDetail;
import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.entity.MovieSeatCount;

public interface MovieMapper {
    List<MovieListItem> findAllWithGenres();
    List<MovieListItem> searchByTitle(@Param("keyword") String keyword);
    List<MovieListItem> searchByGenreName(@Param("genreName") String genreName);
    int insert(Movie movie);

    List<Movie> findAll();

    Movie findById(@Param("movieId") Integer movieId);

    MovieDetail findMovieDetail(@Param("movieId") Integer movieId);

    /** 영화별 판매 가능 좌석 수 (상영회차 상영관의 활성 좌석 합) */
    List<MovieSeatCount> findAvailableSeatCounts();

    /** 영화별 유효 예매 좌석 수 (booking_status = DONE) */
    List<MovieSeatCount> findSoldSeatCounts();

    List<Movie> findMoviesForDelete(@Param("offset") int offset);

    int deleteMovieGenres(@Param("movieId") Integer movieId);

    int deleteMovieById(@Param("movieId") Integer movieId);
}
