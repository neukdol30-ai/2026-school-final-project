package com.foodlogistics.erp.outbound.dto;

import java.time.LocalDateTime;

public record OutboundResponseDto(

        Long outboundId,

        String outboundNo,

        Long salesOrderId,

        String salesOrderNo,

        Long warehouseId,

        String warehouseName,

        String status,

        String cancelReason,

        LocalDateTime cancelledAt
) {
}
