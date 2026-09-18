package com.kyobo.server.service;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.mapper.MovieMapper;

public class MovieService {
    public List<MovieListItem> getMovieList() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(MovieMapper.class).findAllWithGenres();
        }
    }
}
