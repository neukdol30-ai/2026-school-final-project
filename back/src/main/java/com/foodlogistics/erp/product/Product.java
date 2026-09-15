package com.foodlogistics.erp.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Product {

    private Long productId;
    private Long companyId;

    private String productCode;
    private String productName;

    private String lotManagedYn;
    private String taxType;
    private String storageType;
    private String useYn;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}