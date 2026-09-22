package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

// 출고 확정 시 실제 LOT 재고를 차감할 저장된 LOT 배정 정보다.
public record OutboundLotConfirmInfoDto(
        Long outboundItemId,
        Long lotId,
        BigDecimal baseLotQty
) {
}
