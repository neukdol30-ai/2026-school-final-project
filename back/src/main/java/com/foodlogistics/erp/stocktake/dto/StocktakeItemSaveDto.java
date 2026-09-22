package com.foodlogistics.erp.stocktake.dto;

import java.math.BigDecimal;

public record StocktakeItemSaveDto(

        Long productId,

        Long lotId,
        // 조회 수량
        BigDecimal systemQty,
        // 실제 수량
        BigDecimal actualQty,

        // actualQty - systemQty
        BigDecimal differenceQty,

        String reason
) {
}
