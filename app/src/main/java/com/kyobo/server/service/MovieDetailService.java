package com.kyobo.server.service;

import com.kyobo.server.entity.MovieDetail;
import com.kyobo.server.mapper.MovieMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class MovieDetailService {

    private final SqlSessionFactory sqlSessionFactory;

    public MovieDetailService(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public MovieDetail findMovieDetail(Integer movieId) {
        if (movieId == null || movieId <= 0) {
            return null;
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            MovieMapper mapper =
                    session.getMapper(MovieMapper.class);

            return mapper.findMovieDetail(movieId);
        }
    }
}