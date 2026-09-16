package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

public record OutboundItemConfirmInfoDto(

        Long outboundItemId,

        Long salesOrderId,

        Long salesOrderItemId,

        Long warehouseId,

        Long productId,

        String lotManagedYn,

        BigDecimal baseShippedQty,

        BigDecimal baseOrderedQty,

        BigDecimal shippedQty,

        Integer version


) {
}
