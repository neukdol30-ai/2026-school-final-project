package com.foodlogistics.erp.outbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OutboundItemCreateRequestDto(
// 출고 품목 요청 dto
        @NotNull(message = "판매주문 품목 ID는 필수입니다.")
        @Positive(message = "판매주문 품목 ID는 1 이상이어야 합니다.")
        Long salesOrderItemId,

        @NotNull(message = "상품 단위 ID는 필수입니다.")
        @Positive(message = "상품 단위 ID는 1 이상이어야 합니다.")
        Long productUnitId,

        @NotNull(message = "출고 수량은 필수입니다.")
        @Positive(message = "출고 수량은 0보다 커야 합니다.")
        BigDecimal shippedQty
) {
}
