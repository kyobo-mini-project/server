package com.kyobo.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kyobo.server.entity.Movie;
import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.entity.MovieOccupancy;
import com.kyobo.server.mapper.MovieMapper;

import com.kyobo.server.common.ApiResponse;

import com.kyobo.server.mapper.GenreMapper;
import com.kyobo.server.mapper.MovieGenreMapper;

import com.kyobo.server.entity.Genre;

public class MovieService {
    private final MovieOccupancyService movieOccupancyService = new MovieOccupancyService();

    public List<MovieListItem> getMovieList() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            List<MovieListItem> movies = session.getMapper(MovieMapper.class).findAllWithGenres();
            Map<Integer, Double> occupancyByMovie = toOccupancyMap(movieOccupancyService.getAllOccupancies());
            for (MovieListItem movie : movies) {
                movie.setOccupancyRate(occupancyByMovie.getOrDefault(movie.getMovieId(), 0.0));
            }
            return movies;
        }
    }

    public ApiResponse<Void> insertMovie(
            Movie movie, List<Integer> genreIds) {

        if (movie == null || genreIds == null || genreIds.isEmpty()) {
            return ApiResponse.fail(
                    "01", "영화 정보와 장르를 확인해 주세요.");
        }

        // 중복 번호는 한 번만 저장
        List<Integer> selectedGenreIds =
                new java.util.ArrayList<>(
                        new java.util.LinkedHashSet<>(genreIds));

        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            MovieMapper movieMapper =
                    session.getMapper(MovieMapper.class);

            GenreMapper genreMapper =
                    session.getMapper(GenreMapper.class);

            MovieGenreMapper movieGenreMapper =
                    session.getMapper(MovieGenreMapper.class);

            // 1. 선택한 장르가 모두 존재하는지 확인
            List<Genre> genres = genreMapper.findAll();

            for (Integer genreId : selectedGenreIds) {
                if (genreId == null) {
                    return ApiResponse.fail(
                            "01", "장르 번호를 확인해 주세요.");
                }

                boolean exists = false;

                for (Genre genre : genres) {
                    if (genreId.equals(genre.getGenreId())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    return ApiResponse.fail(
                            "01", "존재하지 않는 장르입니다: " + genreId);
                }
            }

            // 2. 영화는 한 번만 저장
            int movieCount = movieMapper.insert(movie);

            if (movieCount != 1 || movie.getMovieId() == null) {
                session.rollback();
                return ApiResponse.error("server error occured");
            }

            // 3. 선택한 장르마다 연결 정보 저장
            for (Integer genreId : selectedGenreIds) {
                int genreCount = movieGenreMapper.insert(
                        movie.getMovieId(), genreId);

                if (genreCount != 1) {
                    session.rollback();
                    return ApiResponse.error("server error occured");
                }
            }

            // 4. 영화와 모든 장르 연결이 성공하면 저장 확정
            session.commit();
            return ApiResponse.success(null);

        } catch (Exception e) {
            return ApiResponse.error("server error occured");
        }
    }
    public ApiResponse<List<Genre>> getGenreList() {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            GenreMapper genreMapper =
                    session.getMapper(GenreMapper.class);

            List<Genre> genres = genreMapper.findAll();

            return ApiResponse.success(genres);

        } catch (Exception e) {
            return ApiResponse.error("server error occured");
        }
    }

    private static Map<Integer, Double> toOccupancyMap(
            List<MovieOccupancy> occupancies) {

        Map<Integer, Double> map = new HashMap<>();

        for (MovieOccupancy occupancy : occupancies) {
            map.put(
                    occupancy.getMovieId(),
                    occupancy.getOccupancyRate()
            );
        }

        return map;
    }
}
