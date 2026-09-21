package com.kyobo.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.entity.MovieOccupancy;
import com.kyobo.server.mapper.MovieMapper;

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

    private static Map<Integer, Double> toOccupancyMap(List<MovieOccupancy> occupancies) {
        Map<Integer, Double> map = new HashMap<>();
        for (MovieOccupancy occupancy : occupancies) {
            map.put(occupancy.getMovieId(), occupancy.getOccupancyRate());
        }
        return map;
    }
}
