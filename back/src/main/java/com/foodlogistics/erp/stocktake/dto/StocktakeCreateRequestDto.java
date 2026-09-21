package com.foodlogistics.erp.stocktake.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StocktakeCreateRequestDto(

        @NotNull(message = "실사 창고 ID는 필수입니다.")
        @Positive(message = "실사 창고 ID는 1 이상이어야 합니다.")
        Long warehouseId,

        @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
        String memo,

        @NotEmpty(message = "실사 품목은 한 건 이상 필요합니다.")
        List<@Valid StocktakeItemCreateRequestDto> items
) {
}