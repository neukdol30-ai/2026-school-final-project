package com.foodlogistics.erp.salesorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SalesOrderItemCreateRequestDto (
        @NotNull(message = "상품 단위 ID는 필수입니다.")
        Long productUnitId,

        @NotNull(message = "주문 수량은 필수입니다.")
        @Positive(message = "주문 수량은 0보다 커야 합니다.")
        BigDecimal orderedQty
) {

}
