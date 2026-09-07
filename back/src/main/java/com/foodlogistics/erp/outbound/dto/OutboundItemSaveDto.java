package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;


// db에 저장할 값을 담는 dto
public record OutboundItemSaveDto(

        Long salesOrderItemId,

        Long productUnitId,

        BigDecimal shippedQty,

        BigDecimal conversionQty,

        BigDecimal baseShippedQty
) {
}
