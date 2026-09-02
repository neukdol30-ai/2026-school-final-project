package com.foodlogistics.erp.salesorder.dto;

import java.math.BigDecimal;

public record SalesOrderItemSaveDto (
        // ex) 라면
        Long productId,
        //  포장 단위 ex) 라면 1박스
        Long productUnitId,

        // 거래단위 수량 ex) 3박스
        BigDecimal orderedQty,

        // 한 박스에 몇 개 들었는지
        BigDecimal conversionQty,

        // 주문 기준 수량  orderedQty * conversionQty
        BigDecimal baseOrderedQty,

        BigDecimal unitPrice,

        String taxType,

        // unitPrice * orderedQty
        BigDecimal supplyAmount,

        BigDecimal taxAmount,

        BigDecimal totalAmount

) {
}
