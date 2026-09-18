package com.kyobo.server.service;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.config.FieldEncryptor;
import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.Admin;
import com.kyobo.server.mapper.AdminMapper;

public class AdminService {
    public ApiResponse<Admin> findAdminByCode(String code) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            AdminMapper adminMapper = session.getMapper(AdminMapper.class);
            Admin admin = adminMapper.findByCode(code);

            if (admin == null) {
                return ApiResponse.fail("01", "존재하지 않는 관리자 코드입니다.");
            }
            return ApiResponse.success(admin);
        } catch (Exception e) {
            return ApiResponse.error("서버 오류가 발생했습니다.");
        }
    }

    public ApiResponse<Void> verifyAdminPassword(String rawPassword, String hashedPassword) {
        try {
            if (FieldEncryptor.matchesPassword(rawPassword, hashedPassword)) {
                return ApiResponse.success(null);
            }
            return ApiResponse.fail("02", "비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            return ApiResponse.error("서버 오류가 발생했습니다.");
        }
    }
}
