package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

// Mapper가 SELECT 결과를 담아서 Service에 전달하는 DTO
public record OutboundItemOrderInfoDto(

        Long companyId,

        Long salesOrderId,

        String orderStatus,

        Long productId,

        BigDecimal baseOrderedQty,

        BigDecimal shippedQty,

        BigDecimal conversionQty
) {
}
