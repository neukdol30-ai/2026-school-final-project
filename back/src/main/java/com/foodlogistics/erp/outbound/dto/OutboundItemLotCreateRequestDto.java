package com.foodlogistics.erp.outbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// LOT 관리 상품을 출고할 때, 어느 LOT에서 얼마를 출고할지 받는 요청 DTO다.
public record OutboundItemLotCreateRequestDto(
        @NotNull(message = "LOT ID는 필수입니다.")
        @Positive(message = "LOT ID는 1 이상이어야 합니다.")
        Long lotId,

        @NotNull(message = "LOT 출고 수량은 필수입니다.")
        @Positive(message = "LOT 출고 수량은 0보다 커야 합니다.")
        BigDecimal baseLotQty
) {
}
