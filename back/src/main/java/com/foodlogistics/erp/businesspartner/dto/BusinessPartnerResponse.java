package com.foodlogistics.erp.businesspartner.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class BusinessPartnerResponse {

    private final Long partnerId;
    private final Long companyId;

    private final String partnerCode;
    private final String partnerName;
    private final String businessNumber;
    private final String representativeName;
    private final String contactName;
    private final String phone;
    private final String email;
    private final String postalCode;
    private final String address1;
    private final String address2;

    private final String supplierYn;
    private final String customerYn;
    private final String useYn;

    private final LocalDateTime createdAt;
    private final Long createdBy;
    private final LocalDateTime updatedAt;
    private final Long updatedBy;
}