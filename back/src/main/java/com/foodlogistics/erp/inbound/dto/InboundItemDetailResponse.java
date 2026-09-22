package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// 입고 상세조회에서 저장된 입고품목 한 줄을 React로 전달하는 응답 DTO
public class InboundItemDetailResponse {

    private Long inboundItemId;

    private Long purchaseOrderItemId;

    private Long productId;

    private String productCode;

    private String productName;

    // Y = LOT 관리 / N = LOT 비관리
    private String lotManagedYn;

    private Long productUnitId;

    private String unitCode;

    private String unitName;

    // 사용자가 DRAFT에 입력해 저장한 입고수량
    private BigDecimal receivedQty;

    // DRAFT 저장 당시의 단위 환산수량 Snapshot
    private BigDecimal conversionQty;

    // receivedQty × conversionQty로 저장된 기준입고수량
    private BigDecimal baseReceivedQty;

    // 현재 PURCHASE_ORDER_ITEM 기준으로 아직 확정 입고되지 않은 기준수량
    private BigDecimal remainingBaseQty;

    private List<InboundItemLotDetailResponse> lots;
}
