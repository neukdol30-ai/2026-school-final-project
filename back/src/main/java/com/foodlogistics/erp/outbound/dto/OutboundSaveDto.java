package com.foodlogistics.erp.outbound.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/*
 * OUTBOUND 테이블에 저장할 출고서 헤더 정보다.
 *
 * 저장 전에는 outboundId가 null이다.
 * DB INSERT 후 MyBatis가 생성된 outboundId를 setOutboundId(...)로 넣어 준다.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class OutboundSaveDto {

    private Long outboundId;



    private final Long companyId;

    private final String outboundNo;


    private final Long salesOrderId;


    private final Long warehouseId;

    private final Long createdBy;
}