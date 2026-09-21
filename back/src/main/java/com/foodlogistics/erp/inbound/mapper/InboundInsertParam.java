package com.foodlogistics.erp.inbound.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// Service에서 검증·생성한 INBOUND Header 값을 INSERT SQL로 전달하는 객체
public class InboundInsertParam {

    private Long inboundId;

    private Long companyId;

    private String inboundNo;

    private Long purchaseOrderId;

    private Long warehouseId;

    private LocalDate inboundDate;

    private String memo;

    private Long createdBy;
}