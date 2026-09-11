package com.foodlogistics.erp.stocktake.dto;

import java.math.BigDecimal;


// 재고실사 헤더와 품목 목록을 함께 반환하는 상세 조회 응답값
public record StocktakeItemResponseDto(
        Long stocktakeItemId,
        Integer lineNo,
        Long productId,
        String productName,
        Long lotId,
        String lotNo,
        BigDecimal systemQty,
        BigDecimal actualQty,
        BigDecimal differenceQty,
        String reason
) {
}