package com.foodlogistics.erp.warehouse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Warehouse {

    private Long warehouseId;
    private Long companyId;

    private String warehouseCode;
    private String warehouseName;

    private String postalCode;
    private String address1;
    private String address2;
    private String description;

    private String useYn;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}
