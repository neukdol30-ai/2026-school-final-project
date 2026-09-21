package com.foodlogistics.erp.stocktake.dto;

// 재고실사 등록 화면의 창고 선택 목록 한 건
public record StocktakeWarehouseOptionDto(
        Long warehouseId,
        String warehouseName
) {
}
