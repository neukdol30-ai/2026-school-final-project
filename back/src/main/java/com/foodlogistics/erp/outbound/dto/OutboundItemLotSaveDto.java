package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

// OUTBOUND_ITEM_LOT 테이블에 저장할 LOT별 기준단위 출고 수량이다.
public record OutboundItemLotSaveDto(
        Long lotId,
        BigDecimal baseLotQty
) {
}
