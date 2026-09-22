package com.foodlogistics.erp.outbound.dto;

// 출고 등록 화면의 창고 선택 목록 한 건
public record OutboundWarehouseOptionDto(
        Long warehouseId,
        String warehouseName
) {
}
