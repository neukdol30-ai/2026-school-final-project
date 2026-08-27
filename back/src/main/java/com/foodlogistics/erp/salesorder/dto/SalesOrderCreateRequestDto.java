package com.foodlogistics.erp.salesorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SalesOrderCreateRequestDto(

        @NotNull(message = "고객 거래처 ID는 필수입니다.")
        Long customerId,

    @NotEmpty(message = "주문 품목은 최소 한 개 이상 입력해야 합니다.")

    @Valid
    List<SalesOrderItemCreateRequestDto> items
) {
}

