package com.foodlogistics.erp.salesorder.dto;

import java.math.BigDecimal;

public record SalesOrderItemOrderInfoDto(

    Long companyId,

    Long productId,

    BigDecimal conversionQty,

    String taxType
) {
}

