package com.kyobo.server.mapper;

import java.util.List;

import com.kyobo.server.entity.Movie;
import com.kyobo.server.entity.MovieListItem;

public interface MovieMapper {
    List<MovieListItem> findAllWithGenres();
    int insert(Movie movie);
}
