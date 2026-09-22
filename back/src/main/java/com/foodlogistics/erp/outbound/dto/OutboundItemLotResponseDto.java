package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

// 출고 초안에 저장된 LOT별 배정 수량 한 건
public record OutboundItemLotResponseDto(
        Long outboundItemId,
        Long lotId,
        BigDecimal baseLotQty
) {
}
