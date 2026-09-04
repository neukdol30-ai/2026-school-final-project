package com.foodlogistics.erp.security.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CurrentUserResponse {

    private final Long appUserId;
    private final Long companyId;
    private final String loginId;
    private final String userName;
    private final List<String> authorities;
}