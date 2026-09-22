package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Admin {
    private Integer cinemaId;
    private String cinemaName;
    private String adminPassword;
}
