package com.foodlogistics.erp.purchase.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// 발주품목 등록 전에 PRODUCT와 PRODUCT_UNIT에서 읽어올 기준정보를 잠시 담는 내부용 객체.
public class PurchaseOrderItemReference {

    private Long productId;

    private Long productUnitId;

    private BigDecimal conversionQty;

    private String taxType;
}
