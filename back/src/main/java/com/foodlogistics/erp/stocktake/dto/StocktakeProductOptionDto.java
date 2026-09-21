package com.foodlogistics.erp.stocktake.dto;

// 재고실사 등록 화면의 상품 선택 목록 한 건
public record StocktakeProductOptionDto(
        Long productId,
        String productName,
        String lotManagedYn
) {
}
