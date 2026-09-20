package com.kyobo.server.service;

import java.util.List;

import com.kyobo.server.entity.Movie;
import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.mapper.MovieMapper;

import com.kyobo.server.common.ApiResponse;

public class MovieService {
    public List<MovieListItem> getMovieList() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(MovieMapper.class).findAllWithGenres();
        }
    }

    public ApiResponse<Void> insertMovie(Movie movie) {
        try (SqlSession session =
                     MyBatisConfig.sqlSessionFactory().openSession()) {

            MovieMapper mapper = session.getMapper(MovieMapper.class);

            int count = mapper.insert(movie);

            if (count != 1) {
                session.rollback();
                return ApiResponse.fail("01", "영화 등록에 실패했습니다.");
            }

            session.commit();
            return ApiResponse.success(null);

        } catch (Exception e) {
            return ApiResponse.error("server error occured");
        }
    }
}
