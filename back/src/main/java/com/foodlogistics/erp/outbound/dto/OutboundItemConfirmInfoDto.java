package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

public record OutboundItemConfirmInfoDto(

        Long salesOrderId,

        Long salesOrderItemId,

        BigDecimal baseShippedQty,

        BigDecimal baseOrderedQty,

        BigDecimal shippedQty,

        Integer version


) {
}
