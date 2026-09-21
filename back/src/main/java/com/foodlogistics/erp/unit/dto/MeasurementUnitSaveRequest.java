package com.foodlogistics.erp.unit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeasurementUnitSaveRequest {

    @NotBlank
    @Size(max = 20)
    private String unitCode;

    @NotBlank
    @Size(max = 50)
    private String unitName;

    @Size(max = 200)
    private String description;
}