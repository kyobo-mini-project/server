package com.kyobo.server.mapper;

import org.apache.ibatis.annotations.Param;

public interface MovieGenreMapper {

    int insert(
            @Param("movieId") Integer movieId,
            @Param("genreId") Integer genreId);
}
