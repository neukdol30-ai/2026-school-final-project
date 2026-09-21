package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
// DRAFT 입고품목 전체 저장 성공 결과 DTO
public class InboundItemsUpdateResponse {

    private final Long inboundId;

    private final int savedItemCount;

    private final int savedLotCount;
}