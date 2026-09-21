package com.foodlogistics.erp.inbound.mapper;

import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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

    // 입고품목 수정 전 INBOUND 기본정보 조회
    InboundItemUpdateTargetInfo
    findInboundItemUpdateTarget(
            @Param("companyId") Long companyId,
            @Param("inboundId") Long inboundId
    );

    // 입고 처리 전 PURCHASE_ORDER Header 잠금
    InboundPurchaseOrderLockInfo
    findPurchaseOrderForUpdate(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 입고품목 수정 전 INBOUND Header 잠금
    InboundItemUpdateTargetInfo
    findInboundForUpdate(
            @Param("companyId") Long companyId,
            @Param("inboundId") Long inboundId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 아직 입고할 잔량이 남은 발주품목 개수 조회
    int countRemainingPurchaseOrderItems(
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 같은 발주의 가장 최근 CONFIRMED(입고확정) 입고일 조회
    LocalDate findLatestConfirmedInboundDate(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // 같은 발주에 존재하는 DRAFT(작성중) 입고서 개수 조회
    int countDraftInbounds(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId
    );

    // DRAFT에 저장할 발주품목의 서버 기준정보 조회
    InboundPurchaseOrderItemResponse
    findInboundPurchaseOrderItem(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("purchaseOrderItemId") Long purchaseOrderItemId
    );

    // 기존 LOT 조회
    LotInfo
    findLot(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("lotNo") String lotNo
    );

    // 신규 LOT 저장
    int insertLot(
            LotInsertParam param
    );

    // 기존 DRAFT의 LOT 연결 전체 삭제
    int deleteInboundItemLots(
            @Param("companyId") Long companyId,
            @Param("inboundId") Long inboundId
    );

    // 기존 DRAFT의 입고품목 전체 삭제
    int deleteInboundItems(
            @Param("companyId") Long companyId,
            @Param("inboundId") Long inboundId
    );

    // 새 입고품목 저장
    int insertInboundItem(
            InboundItemInsertParam param
    );

    // 새 입고품목과 LOT 연결 저장
    int insertInboundItemLot(
            @Param("inboundItemId") Long inboundItemId,
            @Param("lotId") Long lotId,
            @Param("baseLotQty") BigDecimal baseLotQty
    );

    // 입고확정 대상 품목과 LOT 검증정보 조회
    List<InboundConfirmItemInfo>
    findInboundConfirmItems(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("inboundId") Long inboundId
    );

    // 발주품목의 누적 입고수량 증가
    int increasePurchaseOrderItemReceivedQty(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("purchaseOrderItemId") Long purchaseOrderItemId,
            @Param("baseReceivedQty") BigDecimal baseReceivedQty
    );

    // 발주의 입고진행상태 변경
    int updatePurchaseOrderReceiptStatus(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("receiptStatus") String receiptStatus,
            @Param("updatedBy") Long updatedBy
    );

    // INBOUND 확정 처리
    int confirmInbound(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("inboundId") Long inboundId,
            @Param("confirmedBy") Long confirmedBy
    );

    // 입고번호용 Oracle Sequence 다음 값 조회
    Long nextInboundNoSequence();

    // INBOUND Header 저장
    int insertInbound(
            InboundInsertParam param
    );

    // 입고확정 LOT별 재고 반영 정보 조회
    List<InboundConfirmLotInfo> findInboundConfirmLots(
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("inboundId") Long inboundId
    );

    // 기존 LOT 재고 증가
    int increaseInboundLotStock(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("baseLotQty") BigDecimal baseLotQty
    );

    // 최초 LOT 재고 생성
    int insertInboundLotStock(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("baseLotQty") BigDecimal baseLotQty
    );

    // 기존 전체 재고 증가
    int increaseInboundStock(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("baseReceivedQty") BigDecimal baseReceivedQty
    );

    // 최초 전체 재고 생성
    int insertInboundStock(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("baseReceivedQty") BigDecimal baseReceivedQty
    );

    // 입고 재고이력 저장
    int insertInboundStockHistory(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("changeQty") BigDecimal changeQty,
            @Param("inboundId") Long inboundId,
            @Param("createdBy") Long createdBy
    );
}