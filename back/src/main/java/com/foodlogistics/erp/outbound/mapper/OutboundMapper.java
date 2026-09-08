package com.foodlogistics.erp.outbound.mapper;


import com.foodlogistics.erp.outbound.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OutboundMapper {

    OutboundResponseDto findById(
            @Param("outboundId") Long outboundId
    );

    List<OutboundItemConfirmInfoDto> findItemConfirmInfosByOutboundId(
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

    int insertOutbound(OutboundSaveDto outbound);

    int insertOutboundItem(
            @Param("outboundId") Long outboundId,
            @Param("lineNo") int lineNo,
            @Param("item")OutboundItemSaveDto item,
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

    List<OutboundResponseDto> findAllByCompanyId(
            @Param("companyId") Long companyId
    );

    List<OutboundItemResponseDto> findItemsByOutboundId(
            @Param("outboundId") Long outboundId
    );
}
