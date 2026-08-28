package com.foodlogistics.erp.security.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CurrentUserResponse {

    private final Long appUserId;
    private final Long companyId;
    private final String loginId;
    private final String userName;
}
