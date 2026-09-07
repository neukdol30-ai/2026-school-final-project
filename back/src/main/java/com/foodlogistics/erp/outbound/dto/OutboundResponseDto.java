package com.foodlogistics.erp.outbound.dto;

public record OutboundResponseDto(

        Long outboundId,

        String outboundNo,

        Long salesOrderId,

        String salesOrderNo,

        String warehouseName,

        String status
) {
}
