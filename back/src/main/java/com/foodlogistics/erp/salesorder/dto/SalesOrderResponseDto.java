package com.foodlogistics.erp.salesorder.dto;

public record SalesOrderResponseDto(
    Long salesOrderId,
    String orderNo,
    Long customerId,
    String customerName,
    String orderStatus,
    String shipmentStatus
) {
}
