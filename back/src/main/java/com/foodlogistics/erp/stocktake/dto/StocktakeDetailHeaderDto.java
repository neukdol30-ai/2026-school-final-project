package com.foodlogistics.erp.stocktake.dto;

import java.time.LocalDate;

// 재고실사 상세 조회에서 헤더 SQL 결과만 담는 내부 DTO
public record StocktakeDetailHeaderDto(
        Long stocktakeId,
        String stocktakeNo,
        Long warehouseId,
        String warehouseName,
        LocalDate stocktakeDate,
        String status,
        String memo
) {
}
