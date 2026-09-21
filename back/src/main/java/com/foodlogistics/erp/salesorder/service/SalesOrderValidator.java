package com.foodlogistics.erp.salesorder.service;


import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemOrderInfoDto;
import com.foodlogistics.erp.salesorder.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SalesOrderValidator {

    private final SalesOrderMapper salesOrderMapper;

    public void validateUsableCustomer(Long companyId, Long customerId) {
        if(salesOrderMapper.countUsableCustomer(companyId, customerId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "주문 가능한 고객 거래처가 아닙니다."
            );
        }
    }
    public void validateNoDuplicateProductUnitIds(
            List<SalesOrderItemCreateRequestDto> items
    ) {
        Set<Long> usedProductUnitIds = new HashSet<>();

        for(SalesOrderItemCreateRequestDto item : items) {
            if(!usedProductUnitIds.add(item.productUnitId())) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "같은 상품 단위는 주문 품목에 한 번만 등록할 수 있습니다."
                );
            }
        }
    }
    public SalesOrderItemOrderInfoDto getUsableOrderItemInfo(
            Long companyId,
            Long productUnitId
    ) {
        SalesOrderItemOrderInfoDto orderItemInfo =
                salesOrderMapper.findOrderItemInfoByProductUnitId(productUnitId);

        if(orderItemInfo == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "사용 가능한 상품 단위가 아닙니다."
            );
        }

        if(!companyId.equals(orderItemInfo.companyId())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "현재 회사의 상품이 아닙니다."
            );
        }

        return orderItemInfo;
    }
}
