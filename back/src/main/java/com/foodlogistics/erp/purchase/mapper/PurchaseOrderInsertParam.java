package com.foodlogistics.erp.purchase.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// Service가 계산, 검증한 값을 PURCHASE_ORDER INSERT SQL로 넘기기 위한 객체
public class PurchaseOrderInsertParam {

    private Long purchaseOrderId;

    private Long companyId;

    private String orderNo;

    private Long supplierId;

    private Long warehouseId;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private String requestNote;

    private String internalMemo;

    private BigDecimal totalSupplyAmount;

    private BigDecimal totalTaxAmount;

    private BigDecimal totalAmount;

    private Long createdBy;
}
