package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// INBOUND_ITEM INSERT용 내부 객체
public class InboundItemInsertParam {

    private Long inboundItemId;

    private Long inboundId;

    private Long purchaseOrderItemId;

    private Long productUnitId;

    private BigDecimal receivedQty;

    private BigDecimal conversionQty;

    private BigDecimal baseReceivedQty;
}