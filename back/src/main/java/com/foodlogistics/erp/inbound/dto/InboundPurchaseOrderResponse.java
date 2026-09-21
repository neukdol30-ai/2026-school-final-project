package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// 입고 처리가 가능한 발주 한 건을 React로 전달하는 응답 DTO
public class InboundPurchaseOrderResponse {

    private Long purchaseOrderId;

    private String orderNo;

    private Long supplierId;

    private String supplierName;

    private Long warehouseId;

    private String warehouseName;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    // NOT_RECEIVED(미입고) / PARTIAL(부분입고)
    private String receiptStatus;
}