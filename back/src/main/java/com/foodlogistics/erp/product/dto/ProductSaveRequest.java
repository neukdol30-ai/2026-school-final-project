package com.foodlogistics.erp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductSaveRequest {

    @NotBlank
    @Size(max = 50)
    private String productCode;

    @NotBlank
    @Size(max = 100)
    private String productName;

    @NotBlank
    @Pattern(regexp = "^(Y|N)$")
    private String lotManagedYn;

    @NotBlank
    @Pattern(regexp = "^(TAXABLE|TAX_FREE)$")
    private String taxType;

    @NotBlank
    @Pattern(regexp = "^(AMBIENT|CHILLED|FROZEN)$")
    private String storageType;
}