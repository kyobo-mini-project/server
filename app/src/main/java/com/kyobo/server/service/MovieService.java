package com.kyobo.server.service;

import java.util.List;

import com.kyobo.server.entity.Movie;
import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.mapper.MovieMapper;

import com.kyobo.server.common.ApiResponse;

import com.kyobo.server.mapper.GenreMapper;
import com.kyobo.server.mapper.MovieGenreMapper;

import com.kyobo.server.entity.Genre;

public class MovieService {
    public List<MovieListItem> getMovieList() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(MovieMapper.class).findAllWithGenres();
        }
    }

    public ApiResponse<Void> insertMovie(Movie movie, Integer genreId) {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            MovieMapper movieMapper =
                    session.getMapper(MovieMapper.class);

            GenreMapper genreMapper =
                    session.getMapper(GenreMapper.class);

            MovieGenreMapper movieGenreMapper =
                    session.getMapper(MovieGenreMapper.class);

            // 1. 선택한 장르가 DB에 존재하는지 확인
            boolean genreExists = genreMapper.findAll().stream()
                    .anyMatch(genre -> genre.getGenreId().equals(genreId));

            if (!genreExists) {
                return ApiResponse.fail("02", "존재하지 않는 장르입니다.");
            }

            // 2. 영화 저장
            int movieCount = movieMapper.insert(movie);

            if (movieCount != 1) {
                session.rollback();
                return ApiResponse.fail("01", "영화 등록에 실패했습니다.");
            }

            // 3. DB가 생성한 영화번호 확인
            Integer movieId = movie.getMovieId();

            if (movieId == null) {
                session.rollback();
                return ApiResponse.error("server error occured");
            }

            // 4. 영화와 선택한 장르 연결 저장
            int genreCount = movieGenreMapper.insert(movieId, genreId);

            if (genreCount != 1) {
                session.rollback();
                return ApiResponse.fail("03", "영화 장르 연결에 실패했습니다.");
            }

            // 5. 두 저장 작업을 모두 확정
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
}
