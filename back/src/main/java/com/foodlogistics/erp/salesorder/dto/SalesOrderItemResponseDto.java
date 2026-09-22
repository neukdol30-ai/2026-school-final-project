package com.foodlogistics.erp.salesorder.dto;

import java.math.BigDecimal;

public record SalesOrderItemResponseDto (
        Long salesOrderItemId,
        Long productUnitId,
        Long productId,
        String productName,
        String lotManagedYn,
        BigDecimal conversionQty,
        BigDecimal orderedQty,
        BigDecimal unitPrice,
        BigDecimal shippedQty,
        BigDecimal remainingQty
) {

}
