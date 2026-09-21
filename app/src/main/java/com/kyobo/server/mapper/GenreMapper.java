package com.kyobo.server.mapper;

import java.util.List;

import com.kyobo.server.entity.Genre;

    public interface GenreMapper {
        List<Genre> findAll();
    }

