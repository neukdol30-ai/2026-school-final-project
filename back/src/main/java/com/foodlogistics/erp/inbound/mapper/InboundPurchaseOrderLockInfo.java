package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// 입고 DRAFT 생성 시 잠근 발주 Header의 검증용 정보
public class InboundPurchaseOrderLockInfo {

    private Long purchaseOrderId;

    private Long companyId;

    private Long warehouseId;

    private LocalDate orderDate;

    private String approvalStatus;

    private String receiptStatus;
}