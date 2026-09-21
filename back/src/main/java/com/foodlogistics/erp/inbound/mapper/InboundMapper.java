package com.foodlogistics.erp.inbound.mapper;

import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}