package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

public record OutboundItemResponseDto(
        Long outboundItemId,
        Integer lineNo,
        Long salesOrderItemId,
        Long productUnitId,
        BigDecimal shippedQty,
        BigDecimal conversionQty,
        BigDecimal baseShippedQty
) {
}
