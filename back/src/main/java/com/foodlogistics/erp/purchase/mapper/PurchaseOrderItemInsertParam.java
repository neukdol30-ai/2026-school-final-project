package com.foodlogistics.erp.purchase.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// 발주품목 한 줄을 PURCHASE_ORDER_ITEM에 INSERT하기 위해 Service에서 Mapper로 넘길 내부 객체.
public class PurchaseOrderItemInsertParam {

    private Long purchaseOrderId;

    private Long productId;

    private Long productUnitId;

    private BigDecimal orderedQty;

    private BigDecimal conversionQty;

    private BigDecimal baseOrderedQty;

    private BigDecimal unitPrice;

    private String taxType;

    private BigDecimal supplyAmount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;
}
