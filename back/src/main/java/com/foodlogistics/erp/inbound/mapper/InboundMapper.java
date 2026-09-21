package com.foodlogistics.erp.inbound.mapper;

import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface InboundMapper {

    // 현재 회사의 입고 대상 발주 목록 조회
    List<InboundPurchaseOrderResponse>
    findInboundTargetPurchaseOrders(
            @Param("companyId") Long companyId
    );

    // 선택한 발주의 입고 가능 품목 조회
    List<InboundPurchaseOrderItemResponse>
    findInboundTargetPurchaseOrderItems(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 입고 DRAFT 생성 전 발주 Header 잠금
    InboundPurchaseOrderLockInfo
    findPurchaseOrderForUpdate(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 아직 입고할 잔량이 남은 발주품목 개수 조회
    int countRemainingPurchaseOrderItems(
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 같은 발주의 가장 최근 CONFIRMED 입고일 조회
    LocalDate findLatestConfirmedInboundDate(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 같은 발주에 존재하는 DRAFT 입고서 개수 조회
    int countDraftInbounds(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 입고번호용 Oracle Sequence 다음 값 조회
    Long nextInboundNoSequence();

    // INBOUND Header 저장
    int insertInbound(
            InboundInsertParam param
    );
}