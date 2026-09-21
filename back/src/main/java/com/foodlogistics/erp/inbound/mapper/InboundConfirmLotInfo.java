package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class InboundConfirmLotInfo {

    private Long inboundItemId;

    private Long productId;

    private Long lotId;

    private BigDecimal baseLotQty;
}