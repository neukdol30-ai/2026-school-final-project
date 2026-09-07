package com.foodlogistics.erp.outbound.service;


import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.outbound.dto.OutboundItemCreateRequestDto;
import com.foodlogistics.erp.outbound.dto.OutboundItemOrderInfoDto;
import com.foodlogistics.erp.outbound.dto.OutboundItemSaveDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OutboundCalculator {

    public OutboundItemSaveDto calculateItem(
            OutboundItemCreateRequestDto item,
            OutboundItemOrderInfoDto orderInfo
    ) {
        BigDecimal baseShippedQty = calculateBaseQuantity(
                item.shippedQty(),
                orderInfo.conversionQty()
        );

        validateRemainingQuantity(baseShippedQty,orderInfo);

        return new OutboundItemSaveDto(
                item.salesOrderItemId(),
                item.productUnitId(),
                item.shippedQty(),
                orderInfo.conversionQty(),
                baseShippedQty
        );
    }

    private BigDecimal calculateBaseQuantity(
            BigDecimal shippedQty,
            BigDecimal conversionQty

    ) {
        try {
            return shippedQty
                    .multiply(conversionQty)
                    .setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "출고 수량 환산 결과는 소수점 셋째 자리까지만 정확히 입력할 수 있습니다."
            );
        }
    }

    private void validateRemainingQuantity(
            BigDecimal baseShippedQty,
            OutboundItemOrderInfoDto orderInfo
    )  {
        BigDecimal remainingQty = orderInfo.baseOrderedQty()
                .subtract(orderInfo.shippedQty());

        if(baseShippedQty.compareTo(remainingQty) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "남은 출고 가능 수량을 초과했습니다."
            );
        }
    }



}
