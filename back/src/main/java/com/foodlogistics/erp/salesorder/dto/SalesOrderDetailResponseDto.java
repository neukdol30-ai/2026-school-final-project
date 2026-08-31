package com.foodlogistics.erp.salesorder.dto;

import java.util.List;

public record SalesOrderDetailResponseDto (
        Long salesOrderId,
        String orderNo,
        String customerName,
        String orderStatus,
        String shipmentStatus,
        List<SalesOrderItemResponseDto> items
){
}
