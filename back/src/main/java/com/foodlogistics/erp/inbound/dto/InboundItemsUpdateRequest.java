package com.foodlogistics.erp.inbound.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// DRAFT 입고서의 품목 전체 교체 저장 요청 DTO
public class InboundItemsUpdateRequest {

    @NotNull(message = "입고 품목 목록은 null일 수 없습니다.")
    @Valid
    private List<InboundItemUpdateRequest> items;
}