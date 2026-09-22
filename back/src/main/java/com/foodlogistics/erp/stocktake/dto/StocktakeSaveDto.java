package com.foodlogistics.erp.stocktake.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StocktakeSaveDto {

    private final Long companyId;
    private final String stocktakeNo;
    private final Long warehouseId;
    private final String memo;
    private final Long createdBy;
    private Long stocktakeId;
}
