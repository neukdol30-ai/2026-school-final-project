package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// 입고확정 전 INBOUND_ITEM과 LOT 검증용 정보
public class InboundConfirmItemInfo {

    private Long inboundItemId;

    private Long purchaseOrderItemId;

    private Long productId;

    private String lotManagedYn;

    private BigDecimal baseReceivedQty;

    private int lotCount;

    private BigDecimal baseLotQtyTotal;
}