package com.kyobo.server.service;

import com.kyobo.server.entity.MovieDetail;
import com.kyobo.server.entity.MovieOccupancy;
import com.kyobo.server.mapper.MovieMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class MovieDetailService {

    private final SqlSessionFactory sqlSessionFactory;
    private final MovieOccupancyService movieOccupancyService;

    public MovieDetailService(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.movieOccupancyService = new MovieOccupancyService();
    }

    public MovieDetail findMovieDetail(Integer movieId) {
        if (movieId == null || movieId <= 0) {
            return null;
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            MovieMapper mapper =
                    session.getMapper(MovieMapper.class);

            MovieDetail movie = mapper.findMovieDetail(movieId);
            if (movie == null) {
                return null;
            }

            MovieOccupancy occupancy = movieOccupancyService.getOccupancy(movieId);
            movie.setOccupancyRate(occupancy.getOccupancyRate());
            movie.setOccupancyRank(movieOccupancyService.getOccupancyRank(movieId));
            return movie;
        }
    }
}