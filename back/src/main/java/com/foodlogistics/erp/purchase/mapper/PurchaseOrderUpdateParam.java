package com.foodlogistics.erp.purchase.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// Service가 검증, 계산한 값을 PURCHASE_ORDER UPDATE SQL로 넘기기 위한 객체
public class PurchaseOrderUpdateParam {

    private Long purchaseOrderId;

    private Long companyId;

    private Long supplierId;

    private Long warehouseId;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private String requestNote;

    private String internalMemo;

    private BigDecimal totalSupplyAmount;

    private BigDecimal totalTaxAmount;

    private BigDecimal totalAmount;

    private Long updatedBy;
}
