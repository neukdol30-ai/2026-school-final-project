package com.foodlogistics.erp.security.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthUser {

    private Long appUserId;
    private Long companyId;

    private String loginId;
    private String password;
    private String userName;
    private String useYn;
}
