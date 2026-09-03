package com.foodlogistics.erp.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

    private final Long appUserId;
    private final Long companyId;

    private final String loginId;
    private final String userName;

    private final String email;
    private final String phone;
    private final String positionName;

    private final String useYn;
}
