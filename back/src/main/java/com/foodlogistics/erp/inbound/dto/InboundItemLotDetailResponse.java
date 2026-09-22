package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// 입고 상세조회에서 저장된 LOT 한 건을 React로 전달하는 응답 DTO
public class InboundItemLotDetailResponse {

    private Long inboundItemLotId;

    // Service가 LOT 목록을 해당 입고품목에 묶을 때 사용하는 부모 INBOUND_ITEM ID
    private Long inboundItemId;

    private Long lotId;

    private String lotNo;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    private BigDecimal baseLotQty;
}
