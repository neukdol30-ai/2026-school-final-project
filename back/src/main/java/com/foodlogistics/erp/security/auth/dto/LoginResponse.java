package com.foodlogistics.erp.security.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresIn;

    private final Long appUserId;
    private final Long companyId;

    private final String loginId;
    private final String userName;
}
