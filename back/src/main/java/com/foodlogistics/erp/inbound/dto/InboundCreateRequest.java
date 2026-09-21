package com.foodlogistics.erp.inbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// React가 보내는 INBOUND DRAFT 생성 요청을 받는 DTO
public class InboundCreateRequest {

    @NotNull(message = "발주를 선택해 주십시오.")
    @Positive(message = "발주 ID는 0보다 큰 값이어야 합니다.")
    private Long purchaseOrderId;

    @NotNull(message = "입고일을 입력해 주십시오.")
    private LocalDate inboundDate;

    @Size(max = 500, message = "입고 메모는 500자 이하여야 합니다.")
    private String memo;
}