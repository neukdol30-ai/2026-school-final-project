package com.foodlogistics.erp.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WarehouseSaveRequest {

    @NotBlank
    @Size(max = 30)
    private String warehouseCode;

    @NotBlank
    @Size(max = 100)
    private String warehouseName;

    @Size(max = 10)
    private String postalCode;

    @Size(max = 200)
    private String address1;

    @Size(max = 200)
    private String address2;

    @Size(max = 200)
    private String description;
}