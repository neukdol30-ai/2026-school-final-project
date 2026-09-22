package com.foodlogistics.erp.businesspartner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BusinessPartner {

    private Long partnerId;
    private Long companyId;

    private String partnerCode;
    private String partnerName;
    private String businessNumber;
    private String representativeName;
    private String contactName;
    private String phone;
    private String email;
    private String postalCode;
    private String address1;
    private String address2;

    private String supplierYn;
    private String customerYn;
    private String useYn;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}