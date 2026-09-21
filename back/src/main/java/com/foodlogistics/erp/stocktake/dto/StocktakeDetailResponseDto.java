package com.foodlogistics.erp.stocktake.dto;

import java.time.LocalDate;
import java.util.List;


// 재고실사 품목 한 건의 상세 조회 응답값
public record StocktakeDetailResponseDto(
        Long stocktakeId,
        String stocktakeNo,
        Long warehouseId,
        String warehouseName,
        LocalDate stocktakeDate,
        String status,
        String memo,
        List<StocktakeItemResponseDto> items
) {
}