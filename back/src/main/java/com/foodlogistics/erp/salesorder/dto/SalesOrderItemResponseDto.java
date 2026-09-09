package com.foodlogistics.erp.salesorder.dto;

import java.math.BigDecimal;

public record SalesOrderItemResponseDto (
        Long salesOrderItemId,
        Long productUnitId,
        BigDecimal orderedQty,
        BigDecimal shippedQty,
        BigDecimal remainingQty
) {

}
