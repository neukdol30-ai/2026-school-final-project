package com.foodlogistics.erp.purchase.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// 발주서에 포함되는 상품 한 줄을 표현하는 DTO
public class PurchaseOrderItemCreateRequest {

    @NotNull(message = "상품을 선택해 주십시오.")
    @Positive(message = "상품 ID는 0보다 커야 합니다.")
    private Long productId;

    @NotNull(message = "상품 단위를 선택해 주십시오.")
    @Positive(message = "상품 단위 ID는 0보다 커야 합니다.")
    private Long productUnitId;

    @NotNull(message = "발주수량을 입력해 주십시오.")
    @Positive(message = "발주수량은 0보다 커야 합니다.")
    @Digits(
            integer = 16,
            fraction = 3,
            message = "발주수량은 정수 16자리, 소수점 3자리까지 가능합니다."
    )
    private BigDecimal orderedQty;

    @NotNull(message = "매입단가를 입력해 주십시오.")
    @PositiveOrZero(message = "매입단가는 0 이상이어야 합니다.")
    @Digits(
            integer = 17,
            fraction = 2,
            message = "매입단가는 정수 17자리, 소수점 2자리까지 가능합니다."
    )
    private BigDecimal unitPrice;


}
