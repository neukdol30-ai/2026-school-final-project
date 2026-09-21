package com.foodlogistics.erp.outbound.dto;

import java.math.BigDecimal;

// 출고 등록 화면에서 선택할 수 있는 창고별 LOT 재고다.
public record OutboundLotOptionDto(
        Long lotId,
        String lotNo,
        BigDecimal quantity
) {
}
