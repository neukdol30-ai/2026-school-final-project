package com.foodlogistics.erp.unit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MeasurementUnit {

    private Long unitId;

    private String unitCode;
    private String unitName;
    private String description;

    private String useYn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}