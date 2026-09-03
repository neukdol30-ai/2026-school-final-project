package com.foodlogistics.erp.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    private Long appUserId;
    private Long companyId;

    private String loginId;
    private String password;
    private String userName;

    private String email;
    private String phone;
    private String positionName;

    private String useYn;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}
