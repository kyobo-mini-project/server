package com.kyobo.server.mapper;

import com.kyobo.server.entity.User;

public interface UserMapper {
    User findByLoginId(String loginId);

    int insert(User user);

    User findById(Integer userId);

    int softDelete(Integer userId);
}
