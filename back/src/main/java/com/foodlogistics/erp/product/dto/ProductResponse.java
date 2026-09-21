package com.foodlogistics.erp.product.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ProductResponse {

    private final Long productId;
    private final Long companyId;

    private final String productCode;
    private final String productName;

    private final String lotManagedYn;
    private final String taxType;
    private final String storageType;
    private final String useYn;

    private final LocalDateTime createdAt;
    private final Long createdBy;

    private final LocalDateTime updatedAt;
    private final Long updatedBy;
}