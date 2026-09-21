package com.foodlogistics.erp.stocktake.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record StocktakeItemCreateRequestDto(

        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 1 이상이어야 합니다.")
        Long productId,

        // LOT 비관리 상품은 null, LOT 관리 상품은 선택한 LOT ID를 보낸다.
        @Positive(message = "LOT ID는 1 이상이어야 합니다.")
        Long lotId,

        // 사용자가 실제로 센 수량이다.
        @NotNull(message = "실사 수량은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = true,
                message = "실사 수량은 0 이상이어야 합니다.")
        BigDecimal actualQty,

        String reason
) {
}