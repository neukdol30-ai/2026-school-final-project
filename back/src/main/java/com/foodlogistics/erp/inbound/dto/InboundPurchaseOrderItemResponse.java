package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// 입고 가능한 발주품목 한 줄을 React로 전달하는 응답 DTO
public class InboundPurchaseOrderItemResponse {

    private Long purchaseOrderItemId;

    private Long productId;

    private String productCode;

    private String productName;

    // Y = LOT 관리 / N = LOT 비관리
    private String lotManagedYn;

    private Long productUnitId;

    private String unitCode;

    private String unitName;

    private BigDecimal orderedQty;

    // 발주 당시 저장된 환산수량 Snapshot
    private BigDecimal conversionQty;

    private BigDecimal baseOrderedQty;

    private BigDecimal receivedQty;

    private BigDecimal remainingBaseQty;
}