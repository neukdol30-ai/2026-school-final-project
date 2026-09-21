package com.foodlogistics.erp.inbound.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// INBOUND_ITEM 한 줄에 대응하는 입고수량 요청 DTO
public class InboundItemUpdateRequest {

    @NotNull(message = "발주 품목 ID를 입력해 주십시오.")
    @Positive(message = "발주 품목 ID는 0보다 큰 값이어야 합니다.")
    private Long purchaseOrderItemId;

    @NotNull(message = "입고수량을 입력해 주십시오.")
    @DecimalMin(
            value = "0.001",
            message = "입고수량은 0.001 이상이어야 합니다."
    )
    @Digits(
            integer = 16,
            fraction = 3,
            message = "입고수량은 정수 16자리, 소수 3자리 이하여야 합니다."
    )
    private BigDecimal receivedQty;

    @Valid
    private List<InboundItemLotRequest> lots;
}