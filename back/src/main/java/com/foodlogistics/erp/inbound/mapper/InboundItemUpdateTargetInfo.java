package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// 입고품목 수정 대상 INBOUND의 검증용 정보
public class InboundItemUpdateTargetInfo {

    private Long inboundId;

    private Long companyId;

    private Long purchaseOrderId;

    private String status;
}