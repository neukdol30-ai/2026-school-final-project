package com.foodlogistics.erp.stocktake.dto;

import java.math.BigDecimal;

// 재고실사 확정 시 실제 재고와 재고 이력을 조정하는 데 필요한 품목 정보다.
public record StocktakeConfirmItemDto(
        Long productId,
        Long lotId,
        BigDecimal differenceQty
) {
}
