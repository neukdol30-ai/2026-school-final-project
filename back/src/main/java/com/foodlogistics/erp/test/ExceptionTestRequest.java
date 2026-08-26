package com.foodlogistics.erp.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExceptionTestRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Positive(message = "수량은 0보다 커야 합니다.")
    private Integer quantity;
}
