package com.foodlogistics.erp.masterdata.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class AvailableProductUnitResponse {

    private final Long productUnitId;
    private final String unitCode;
    private final String unitName;
    private final BigDecimal conversionQty;
    private final String isBaseYn;
}