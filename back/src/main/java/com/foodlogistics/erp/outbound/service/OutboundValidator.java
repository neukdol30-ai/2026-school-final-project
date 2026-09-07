package com.foodlogistics.erp.outbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.outbound.dto.OutboundItemCreateRequestDto;
import com.foodlogistics.erp.outbound.dto.OutboundItemOrderInfoDto;
import com.foodlogistics.erp.outbound.mapper.OutboundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OutboundValidator {

    private final OutboundMapper outboundMapper;

    public void validateUsableWarehouse(
            Long companyId,
            Long warehouseId
    ) {
        if(outboundMapper.countUsableWarehouse(companyId,warehouseId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "사용 가능한 출고 창고가 아닙니다."
            );
        }
    }

    public  void validateNoDuplicateSalesOrderItemIds(
            List<OutboundItemCreateRequestDto> items
    ) {
        Set<Long> usedSalesOrderItemIds = new HashSet<>();

        for(OutboundItemCreateRequestDto item : items) {
            if(!usedSalesOrderItemIds.add(item.salesOrderItemId())) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "같은 판매주문 품목은 출고서에 한 번만 등록할 수 있습니다."
                );
            }
        }
    }

    public OutboundItemOrderInfoDto getOutboundItemOrderInfo(
            Long companyId,
            Long salesOrderId,
            OutboundItemCreateRequestDto item
    ) {
        OutboundItemOrderInfoDto orderInfo =
                outboundMapper.findOutboundItemOrderInfo(
                        item.salesOrderItemId(),
                        item.productUnitId()
                );

        if(orderInfo == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "출고할 수 없는 판매주문 품목 또는 상품 단위입니다."
            );
        }

        if(!companyId.equals(orderInfo.companyId())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "현재 회사의 판매주문 품목이 아닙니다."
            );
        }

        if(!salesOrderId.equals(orderInfo.salesOrderId())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "선택한 판매주문에 속한 품목이 아닙니다."
            );
        }
        if(!"CONFIRMED".equals(orderInfo.orderStatus())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "주문확정 상태의 판매주문만 출고할 수 있습니다."
            );
        }
        return orderInfo;
    }
}
