package com.foodlogistics.erp.purchase.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
// 발주 등록이 성공한 뒤 Spring에서 React로 돌려줄 데이터를 표현하는 DTO
public class PurchaseOrderCreateResponse {

    private final Long purchaseOrderId;

    private final String orderNo;

    private final String approvalStatus;

    private final String receiptStatus;
}
