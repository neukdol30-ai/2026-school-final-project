package com.foodlogistics.erp.unit.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MeasurementUnitResponse {

    private final Long unitId;

    private final String unitCode;
    private final String unitName;
    private final String description;

    private final String useYn;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}