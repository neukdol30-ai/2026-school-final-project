package com.foodlogistics.erp.salesorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SalesOrderCreateRequestDto(

        @NotNull(message = "고객 거래처 ID는 필수입니다.")
        @Positive(message = "고객 거래처 ID는 1 이상이어야 합니다.")
        Long customerId,

        @NotNull(message = "창고 ID는 필수입니다.")
        @Positive(message = "창고 ID는 1 이상이어야 합니다.")
        Long warehouseId,

    @NotEmpty(message = "주문 품목은 최소 한 개 이상 입력해야 합니다.")

    @Valid
    List<SalesOrderItemCreateRequestDto> items
) {
}

