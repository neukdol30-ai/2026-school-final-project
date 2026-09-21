package com.foodlogistics.erp.outbound.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OutboundCreateRequestDto(

        @NotNull(message = "판매주문 ID는 필수입니다.")
        @Positive(message = "판매주문 ID는 1 이상이어야 합니다.")
        Long salesOrderId,

        @NotNull(message = "출고 창고 ID는 필수입니다.")
        @Positive(message = "출고 창고 ID는 1 이상이어야 합니다.")
        Long warehouseId,

        @NotEmpty(message = "출고 품목은 한 건 이상 필요합니다.")
        List<@Valid OutboundItemCreateRequestDto> items
) {
}
