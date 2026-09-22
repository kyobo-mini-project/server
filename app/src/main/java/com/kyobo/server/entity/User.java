package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class User {
    private Integer userId;
    private String loginId;
    private String userPw;
    private String name;
    private Integer age;
    /** DB 컬럼 phone_number */
    private String phoneNumber;
    private OffsetDateTime createdAt;
    private Boolean isDeleted;
    private OffsetDateTime deletedAt;
}
