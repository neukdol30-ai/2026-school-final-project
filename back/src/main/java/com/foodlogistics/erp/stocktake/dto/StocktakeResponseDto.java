package com.foodlogistics.erp.stocktake.dto;

import java.time.LocalDate;

public record StocktakeResponseDto(

        Long stocktakeId,

        String stocktakeNo,

        String warehouseName,

        LocalDate stocktakeDate,

        String status
) {
}