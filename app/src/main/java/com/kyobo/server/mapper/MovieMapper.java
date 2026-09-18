package com.kyobo.server.mapper;

import java.util.List;
import com.kyobo.server.entity.MovieDetail;
import org.apache.ibatis.annotations.Param;

import com.kyobo.server.entity.MovieListItem;

public interface MovieMapper {
    List<MovieListItem> findAllWithGenres();
    MovieDetail findMovieDetail(@Param("movieId") Integer movieId);
}
