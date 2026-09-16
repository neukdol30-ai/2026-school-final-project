package com.foodlogistics.erp.outbound.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// OUTBOUND_ITEM 및 OUTBOUND_ITEM_LOT 저장에 필요한 값을 담는다.
@Getter
@Setter
@RequiredArgsConstructor
public class OutboundItemSaveDto {

    private final Long salesOrderItemId;
    private final Long productUnitId;
    private final Long productId;
    private final String lotManagedYn;
    private final BigDecimal shippedQty;
    private final BigDecimal conversionQty;
    private final BigDecimal baseShippedQty;
    private final List<OutboundItemLotSaveDto> lotAssignments;

    // OUTBOUND_ITEM INSERT 뒤 생성된 ID를 MyBatis가 넣어 준다.
    private Long outboundItemId;
}
