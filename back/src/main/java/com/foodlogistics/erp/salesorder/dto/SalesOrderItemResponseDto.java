package com.foodlogistics.erp.salesorder.dto;

import java.math.BigDecimal;

public record SalesOrderItemResponseDto (
        Long productUnitId,
        BigDecimal orderedQty
) {

}
