package com.kyobo.server.service;

import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mindrot.jbcrypt.BCrypt;

import com.kyobo.server.config.FieldEncryptor;
import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.User;
import com.kyobo.server.mapper.UserMapper;
import com.kyobo.server.mapper.BookingMapper;

public class UserService {
    private final SqlSessionFactory sessionFactory;

    public UserService() {
        this.sessionFactory = null;
    }

    public UserService(SqlSessionFactory sessionFactory) {
        this.sessionFactory = java.util.Objects.requireNonNull(sessionFactory);
    }

    private SqlSessionFactory sessionFactory() {
        return sessionFactory != null ? sessionFactory : MyBatisConfig.sqlSessionFactory();
    }

    /** 영문·숫자 각각 1자 이상 포함, 총 8자 이상 (영문·숫자만 허용) */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    /**
     * 존재하지 않는 아이디일 때도 BCrypt 비교 비용을 맞추기 위한 더미 해시.
     * (아이디 존재 여부를 응답 시간으로 추측하기 어렵게 함)
     */
    private static final String DUMMY_PASSWORD_HASH =
            BCrypt.hashpw("timing-dummy", BCrypt.gensalt());

    public User signUp(String loginId, String password, String name, int age, String phoneNumber) {
        requireNotBlank(loginId, "아이디");
        requireNotBlank(password, "비밀번호");
        requireValidPassword(password);
        requireNotBlank(name, "이름");
        requireNotBlank(phoneNumber, "전화번호");
        if (age < 0) {
            throw new IllegalArgumentException("나이는 0 이상으로 입력해 주세요.");
        }

        String trimmedLoginId = loginId.trim();
        String trimmedName = name.trim();
        String trimmedPhone = phoneNumber.trim();

        try (SqlSession session = sessionFactory().openSession()) {
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

    public User signIn(String loginId, String password) {
        requireNotBlank(loginId, "아이디");
        requireNotBlank(password, "비밀번호");

        String trimmedLoginId = loginId.trim();

        try (SqlSession session = sessionFactory().openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = mapper.findByLoginId(trimmedLoginId);

            // BCrypt는 호출마다 해시가 달라지므로 DB에서 비밀번호로 직접 조회하면 안 됨.
            // 유저가 없어도 더미 해시로 matches를 돌려 응답 시간 차이를 줄인다.
            String hashToCheck = user != null ? user.getUserPw() : DUMMY_PASSWORD_HASH;
            boolean passwordMatches = FieldEncryptor.matchesPassword(password, hashToCheck);
            if (user == null || !passwordMatches) {
                throw new IllegalStateException("아이디 또는 비밀번호를 확인해주세요.");
            }

            user.setName(FieldEncryptor.decrypt(user.getName()));
            user.setPhoneNumber(FieldEncryptor.decrypt(user.getPhoneNumber()));
            user.setUserPw(null);
            return user;
        }
    }

    public enum WithdrawResult {
        READY, INVALID_USER, ACTIVE_BOOKINGS, PASSWORD_REQUIRED, PASSWORD_MISMATCH, SUCCESS
    }

    /** 비밀번호 입력 전에 회원 상태와 향후 상영 예매를 확인한다. */
    public WithdrawResult checkWithdraw(Integer userId) {
        return processWithdraw(userId, null, false, false);
    }

    public WithdrawResult verifyWithdraw(Integer userId, String password) {
        return processWithdraw(userId, password, true, false);
    }

    public WithdrawResult withdraw(Integer userId, String password) {
        return processWithdraw(userId, password, true, true);
    }

    private WithdrawResult processWithdraw(Integer userId, String password,
                                           boolean verifyPassword, boolean delete) {
        try (SqlSession session = sessionFactory().openSession(false)) {
            try {
                UserMapper mapper = session.getMapper(UserMapper.class);
                User user = mapper.findById(userId);
                boolean passwordMatches = false;
                if (verifyPassword && password != null && !password.isBlank()) {
                    String hashToCheck = user != null ? user.getUserPw() : DUMMY_PASSWORD_HASH;
                    passwordMatches = FieldEncryptor.matchesPassword(password, hashToCheck);
                }

                WithdrawResult result;
                if (user == null || !Boolean.FALSE.equals(user.getIsDeleted())) {
                    result = WithdrawResult.INVALID_USER;
                } else if (session.getMapper(BookingMapper.class).findActiveBookingsByUserId(userId) > 0) {
                    result = WithdrawResult.ACTIVE_BOOKINGS;
                } else if (verifyPassword && (password == null || password.isBlank())) {
                    result = WithdrawResult.PASSWORD_REQUIRED;
                } else if (verifyPassword && !passwordMatches) {
                    result = WithdrawResult.PASSWORD_MISMATCH;
                } else {
                    result = WithdrawResult.READY;
                }

                if (result != WithdrawResult.READY || !delete) {
                    session.rollback();
                    return result;
                }
                if (mapper.softDelete(userId) != 1) {
                    session.rollback();
                    return WithdrawResult.INVALID_USER;
                }
                session.commit();
                return WithdrawResult.SUCCESS;
            } catch (RuntimeException e) {
                session.rollback();
                throw e;
            }
        }
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을(를) 입력해 주세요.");
        }
    }

    private static void requireValidPassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다.");
        }
    }
}
