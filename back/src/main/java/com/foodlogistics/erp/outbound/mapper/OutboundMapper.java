package com.foodlogistics.erp.outbound.mapper;


import com.foodlogistics.erp.outbound.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OutboundMapper {

    OutboundResponseDto findById(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    List<OutboundWarehouseOptionDto> findWarehouseOptions(
            @Param("companyId") Long companyId
    );

    // 선택한 창고·상품에서 출고할 수 있는 LOT 재고 목록
    List<OutboundLotOptionDto> findLotOptions(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    List<OutboundItemConfirmInfoDto> findItemConfirmInfosByOutboundId(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    // 출고 초안에 저장한 LOT별 배정 수량
    List<OutboundLotConfirmInfoDto> findLotConfirmInfosByOutboundId(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    //출고하려는 판매주문 품목 정보를 조회
    OutboundItemOrderInfoDto findOutboundItemOrderInfo(

            @Param("salesOrderItemId") Long salesOrderItemId,
            @Param("productUnitId") Long productUnitId
    );

    int countUsableWarehouse(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );

    // 해당 상품의 LOT가 선택한 창고에 있고, 배정하려는 수량 이상 남았는지 확인
    int countAvailableLotStock(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("baseLotQty") BigDecimal baseLotQty
    );

    int insertOutbound(OutboundSaveDto outbound);

    int insertOutboundItem(
            @Param("outboundId") Long outboundId,
            @Param("lineNo") int lineNo,
            @Param("item")OutboundItemSaveDto item,
            @Param("createdBy") Long createdBy
            );

    int insertOutboundItemLot(
            @Param("outboundItemId") Long outboundItemId,
            @Param("lot") OutboundItemLotSaveDto lot,
            @Param("createdBy") Long createdBy
    );

    int increaseSalesOrderItemShippedQty(
            @Param("salesOrderItemId") Long salesOrderItemId,
            @Param("baseShippedQty")BigDecimal baseShippedQty,
            @Param("version") Integer version,
            @Param("updatedBy") Long updatedBy
            );

    int confirmOutbound(
            @Param("outboundId") Long outboundId,
            @Param("companyId") Long companyId,
            @Param("confirmedBy") Long confirmedBy
    );

    int recalculateSalesOrderShipmentStatus(
            @Param("salesOrderId") Long salesOrderId,
            @Param("companyId") Long companyId,
            @Param("updatedBy") Long updatedBy
    );

    // 확정된 출고서를 취소 상태로 바꾸고 취소 사유·사용자를 기록한다.
    int cancelOutbound(
            @Param("outboundId") Long outboundId,
            @Param("companyId") Long companyId,
            @Param("cancelledBy") Long cancelledBy,
            @Param("cancelReason") String cancelReason
    );

    // LOT 관리 상품의 실제 LOT 재고를 조건부 차감한다.
    int decreaseLotStockQuantity(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("baseLotQty") BigDecimal baseLotQty
    );

    // 창고·상품별 전체 재고를 조건부 차감한다.
    int decreaseStockQuantity(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("baseShippedQty") BigDecimal baseShippedQty
    );

    // 출고 확정으로 생긴 재고 감소 이력을 남긴다.
    int insertOutboundStockHistory(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("changeQty") BigDecimal changeQty,
            @Param("outboundId") Long outboundId,
            @Param("createdBy") Long createdBy
    );

    // 출고 취소로 원복한 재고는 출고 이력과 구분해 양수 이력으로 남긴다.
    int insertOutboundCancelStockHistory(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("changeQty") BigDecimal changeQty,
            @Param("outboundId") Long outboundId,
            @Param("createdBy") Long createdBy
    );

    // 안전을 위해 이 출고 확정에서 실제 재고 차감 이력이 남았는지 확인한다.
    int countOutboundStockHistory(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    // 출고 취소 시 이전에 차감했던 LOT 재고를 복구한다.
    int increaseLotStockQuantity(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("baseLotQty") BigDecimal baseLotQty
    );

    // 출고 취소 시 이전에 차감했던 전체 재고를 복구한다.
    int increaseStockQuantity(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("baseShippedQty") BigDecimal baseShippedQty
    );

    // 출고 취소 시 판매주문 품목의 누적 출고 수량을 원복한다.
    int decreaseSalesOrderItemShippedQty(
            @Param("salesOrderItemId") Long salesOrderItemId,
            @Param("baseShippedQty") BigDecimal baseShippedQty,
            @Param("version") Integer version,
            @Param("updatedBy") Long updatedBy
    );

    List<OutboundResponseDto> findAllByCompanyId(
            @Param("companyId") Long companyId
    );

    List<OutboundItemResponseDto> findItemsByOutboundId(
            @Param("outboundId") Long outboundId
    );

    List<OutboundItemLotResponseDto> findItemLotsByOutboundId(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    int updateOutboundHeader(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId,
            @Param("salesOrderId") Long salesOrderId,
            @Param("warehouseId") Long warehouseId,
            @Param("updatedBy") Long updatedBy
    );

    int deleteOutboundItemLots(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    int deleteOutboundItems(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );

    int deleteOutbound(
            @Param("companyId") Long companyId,
            @Param("outboundId") Long outboundId
    );
}
