package com.kyobo.server.mapper;

import com.kyobo.server.entity.MovieDetail;
import org.apache.ibatis.annotations.Param;

public interface MovieDetailMapper {

    MovieDetail findMovieDetail(@Param("movieId") Integer movieId);
}