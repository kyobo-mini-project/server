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

import com.kyobo.server.entity.MovieDetail;

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

    public ApiResponse<List<Movie>> findMoviesForDelete(int page) {
        if (page < 1) {
            return ApiResponse.fail("01", "페이지는 1 이상이어야 합니다.");
        }

        int offset = (page - 1) * 5;

        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            MovieMapper mapper = session.getMapper(MovieMapper.class);

            List<Movie> movies = mapper.findMoviesForDelete(offset);

            return ApiResponse.success(movies);

        } catch (Exception e) {
            return ApiResponse.error("영화 목록 조회 중 오류가 발생했습니다.");
        }
    }
    public ApiResponse<MovieDetail> findMovieForDelete(int movieId) {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            MovieMapper mapper = session.getMapper(MovieMapper.class);

            MovieDetail movie = mapper.findMovieDetail(movieId);

            if (movie == null) {
                return ApiResponse.fail("01", "존재하지 않는 영화입니다.");
            }

            return ApiResponse.success(movie);

        } catch (Exception e) {
            return ApiResponse.error("영화 상세 조회 중 오류가 발생했습니다.");
        }
    }

    public ApiResponse<Void> deleteMovie(int movieId) {
        if (movieId <= 0) {
            return ApiResponse.fail("01", "올바른 영화 ID를 입력하세요.");
        }

        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            MovieMapper mapper = session.getMapper(MovieMapper.class);

            // 1. 선택한 영화의 장르 연결 삭제
            mapper.deleteMovieGenres(movieId);

            // 2. 영화 삭제
            int count = mapper.deleteMovieById(movieId);

            if (count != 1) {
                session.rollback();
                return ApiResponse.fail("01", "삭제할 영화가 없습니다.");
            }

            // 3. 두 삭제 작업을 함께 확정
            session.commit();

            return ApiResponse.success(null);

        } catch (Exception e) {
            // 외래 키 제약으로 삭제가 막힌 경우인지 확인
            for (Throwable cause = e;
                 cause != null;
                 cause = cause.getCause()) {

                if (cause instanceof java.sql.SQLException) {
                    java.sql.SQLException sqlException =
                            (java.sql.SQLException) cause;

                    if ("23503".equals(sqlException.getSQLState())) {
                        return ApiResponse.fail(
                                "02",
                                "상영회차나 예매 등 연결된 정보가 있어 삭제할 수 없습니다.");
                    }
                }
            }

            return ApiResponse.error("영화 삭제 중 오류가 발생했습니다.");
        }
    }
}
