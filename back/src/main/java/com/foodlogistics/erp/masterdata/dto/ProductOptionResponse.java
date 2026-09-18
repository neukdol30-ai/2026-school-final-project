package com.foodlogistics.erp.masterdata.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductOptionResponse {

    private final Long productId;
    private final String productCode;
    private final String productName;
    private final String taxType;
}