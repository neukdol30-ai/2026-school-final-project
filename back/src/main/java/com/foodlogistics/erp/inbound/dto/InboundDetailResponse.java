package com.foodlogistics.erp.inbound.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// 입고 한 건의 Header 정보와 저장된 입고품목 목록을 React로 전달하는 응답 DTO
public class InboundDetailResponse {

    private Long inboundId;

    private String inboundNo;

    private Long purchaseOrderId;

    private String orderNo;

    private Long supplierId;

    private String supplierName;

    private Long warehouseId;

    private String warehouseName;

    private LocalDate inboundDate;

    // DRAFT / CONFIRMED / CANCELLED
    private String status;

    private String memo;

    private Long createdBy;

    private LocalDateTime createdAt;

    private List<InboundItemDetailResponse> items;
}
