package com.foodlogistics.erp.productunit.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ProductUnitResponse {

    private final Long productUnitId;

    private final Long productId;
    private final Long unitId;

    private final String unitCode;
    private final String unitName;

    private final BigDecimal conversionQty;
    private final String isBaseYn;
    private final String useYn;

    private final LocalDateTime createdAt;
    private final Long createdBy;

    private final LocalDateTime updatedAt;
    private final Long updatedBy;
}