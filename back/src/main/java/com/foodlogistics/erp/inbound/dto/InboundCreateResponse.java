package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
// INBOUND DRAFT 생성 성공 결과를 React로 전달하는 응답 DTO
public class InboundCreateResponse {

    private final Long inboundId;

    private final String inboundNo;

    private final Long purchaseOrderId;

    private final Long warehouseId;

    private final LocalDate inboundDate;

    private final String status;
}