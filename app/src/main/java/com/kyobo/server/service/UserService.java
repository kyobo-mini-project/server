package com.kyobo.server.service;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.FieldEncryptor;
import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.User;
import com.kyobo.server.mapper.UserMapper;

public class UserService {
    public User signUp(String loginId, String password, String name, int age, String phoneNumber) {
        requireNotBlank(loginId, "아이디");
        requireNotBlank(password, "비밀번호");
        requireNotBlank(name, "이름");
        requireNotBlank(phoneNumber, "전화번호");
        if (age < 0) {
            throw new IllegalArgumentException("나이는 0 이상으로 입력해 주세요.");
        }

        String trimmedLoginId = loginId.trim();
        String trimmedName = name.trim();
        String trimmedPhone = phoneNumber.trim();

        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            if (mapper.findByLoginId(trimmedLoginId) != null) {
                throw new IllegalStateException("이미 사용 중인 아이디입니다.");
            }

            User user = new User();
            user.setLoginId(trimmedLoginId);
            user.setUserPw(FieldEncryptor.hashPassword(password));
            user.setName(FieldEncryptor.encrypt(trimmedName));
            user.setAge(age);
            user.setPhoneNumber(FieldEncryptor.encrypt(trimmedPhone));

            mapper.insert(user);
            session.commit();

            // 화면 표시용으로 평문 이름 반환 (DB에는 암호문 저장됨)
            user.setName(trimmedName);
            user.setPhoneNumber(trimmedPhone);
            user.setUserPw(null);
            return user;
        }
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을(를) 입력해 주세요.");
        }
    }
}
