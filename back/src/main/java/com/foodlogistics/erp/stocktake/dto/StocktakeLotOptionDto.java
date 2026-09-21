package com.foodlogistics.erp.stocktake.dto;

import java.math.BigDecimal;

// 선택한 창고·상품에 속한 LOT 선택 목록 한 건
public record StocktakeLotOptionDto(
        Long lotId,
        String lotNo,
        BigDecimal quantity
) {
}
