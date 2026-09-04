package com.foodlogistics.erp.salesorder.service;

import java.math.BigDecimal;

public record SalesOrderAmountSummary(
        BigDecimal totalSupplyAmount,
        BigDecimal totalTaxAmount,
        BigDecimal totalAmount
) {
}
