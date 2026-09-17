package com.kyobo.server.service;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.User;
import com.kyobo.server.mapper.UserMapper;

public class UserService {
    public User signUp(String loginId, String password, String name, String phoneNumber) {
        requireNotBlank(loginId, "아이디");
        requireNotBlank(password, "비밀번호");
        requireNotBlank(name, "이름");
        requireNotBlank(phoneNumber, "전화번호");

        String trimmedLoginId = loginId.trim();

        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            if (mapper.findByLoginId(trimmedLoginId) != null) {
                throw new IllegalStateException("이미 사용 중인 아이디입니다.");
            }

            User user = new User();
            user.setLoginId(trimmedLoginId);
            user.setUserPw(password);
            user.setName(name.trim());
            user.setPhoneNumber(phoneNumber.trim());

            mapper.insert(user);
            session.commit();
            return user;
        }
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을(를) 입력해 주세요.");
        }
    }
}
