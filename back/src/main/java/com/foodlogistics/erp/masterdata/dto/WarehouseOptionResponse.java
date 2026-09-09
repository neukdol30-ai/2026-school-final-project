package com.foodlogistics.erp.masterdata.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WarehouseOptionResponse {

    private final Long warehouseId;
    private final String warehouseCode;
    private final String warehouseName;
}