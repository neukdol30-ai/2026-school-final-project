package com.foodlogistics.erp.productunit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProductUnitSaveRequest {

    @NotNull
    private Long unitId;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 16, fraction = 3)
    private BigDecimal conversionQty;

    @NotBlank
    @Pattern(regexp = "^(Y|N)$")
    private String isBaseYn;
}