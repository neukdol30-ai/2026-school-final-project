package com.foodlogistics.erp.productunit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductUnit {

    private Long productUnitId;

    private Long productId;
    private Long unitId;

    private String unitCode;
    private String unitName;

    private BigDecimal conversionQty;
    private String isBaseYn;
    private String useYn;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}