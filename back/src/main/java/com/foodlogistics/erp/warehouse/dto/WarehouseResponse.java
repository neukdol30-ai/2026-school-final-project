package com.foodlogistics.erp.warehouse.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class WarehouseResponse {

    private final Long warehouseId;
    private final Long companyId;

    private final String warehouseCode;
    private final String warehouseName;

    private final String postalCode;
    private final String address1;
    private final String address2;
    private final String description;

    private final String useYn;

    private final LocalDateTime createdAt;
    private final Long createdBy;

    private final LocalDateTime updatedAt;
    private final Long updatedBy;
}