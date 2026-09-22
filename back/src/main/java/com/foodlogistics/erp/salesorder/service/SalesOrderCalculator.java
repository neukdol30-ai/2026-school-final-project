package com.foodlogistics.erp.salesorder.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemOrderInfoDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemSaveDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class SalesOrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    public SalesOrderItemSaveDto calculateItem(
            SalesOrderItemCreateRequestDto item,
            SalesOrderItemOrderInfoDto orderItemInfo
    ) {
        BigDecimal baseOrderedQty = calculateBaseQuantity(
                item.orderedQty(),
                orderItemInfo.conversionQty()
        );

        BigDecimal supplyAmount = item.unitPrice()
                .multiply(item.orderedQty())
                .setScale(0, RoundingMode.DOWN);

        BigDecimal taxAmount = "TAXABLE".equals(orderItemInfo.taxType())
                ? supplyAmount.multiply(VAT_RATE).setScale(0, RoundingMode.DOWN)
                : BigDecimal.ZERO;

        BigDecimal totalAmount = supplyAmount.add(taxAmount);

        return new SalesOrderItemSaveDto(
                orderItemInfo.productId(),
                item.productUnitId(),
                item.orderedQty(),
                orderItemInfo.conversionQty(),
                baseOrderedQty,
                item.unitPrice(),
                orderItemInfo.taxType(),
                supplyAmount,
                taxAmount,
                totalAmount
        );
    }
    private BigDecimal calculateBaseQuantity(
            BigDecimal transactionQty,
            BigDecimal conversionQty
    ) {
        try {
            return transactionQty
                    .multiply(conversionQty)
                    .setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "수량 환산 결과는 소수점 셋째 자리까지만 정확히 입력할 수 있습니다."
            );
        }
    }

    public SalesOrderAmountSummary summarize(
            List<SalesOrderItemSaveDto> itemsToSave
    ) {
        BigDecimal totalSupplyAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SalesOrderItemSaveDto itemToSave : itemsToSave) {
            totalSupplyAmount = totalSupplyAmount.add(itemToSave.supplyAmount());
            totalTaxAmount = totalTaxAmount.add(itemToSave.taxAmount());
            totalAmount = totalAmount.add(itemToSave.totalAmount());
        }

        return new SalesOrderAmountSummary(
                totalSupplyAmount,
                totalTaxAmount,
                totalAmount
        );
    }
}