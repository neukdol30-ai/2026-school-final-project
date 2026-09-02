package com.foodlogistics.erp.salesorder.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class SalesOrderSaveDto {

    private Long salesOrderId;

    private final Long companyId;

    private final String orderNo;

    private final Long customerId;

    private final Long warehouseId;

    private final BigDecimal totalSupplyAmount;

    private final BigDecimal totalTaxAmount;

    private final BigDecimal totalAmount;

    private final Long createdBy;


}
