package com.foodlogistics.erp.purchase.calculator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderItemCreateRequest;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemReference;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PurchaseOrderCalculator {

    // 과세상품의 부가가치세율 10%
    private static final BigDecimal VAT_RATE =
            new BigDecimal("0.10");

    // 면세상품의 세액 0원을 NUMBER(19,2)에 맞춘 값
    private static final BigDecimal ZERO_MONEY =
            new BigDecimal("0.00");

    // 과세상품 코드
    private static final String TAXABLE = "TAXABLE";

    // 발주 품목 한 줄의 수량과 금액을 계산
    public PurchaseOrderItemInsertParam calculateItem(
            PurchaseOrderItemCreateRequest item,
            PurchaseOrderItemReference reference
    ) {

        // 거래단위 수량을 기준단위 수량으로 환산
        BigDecimal baseOrderedQty =
                calculateExactBaseQty(
                        item.getOrderedQty(),
                        reference.getConversionQty()
                );

        // 공급가액 = 발주수량 × 매입단가
        BigDecimal supplyAmount =
                truncateWon(
                        item.getOrderedQty()
                                .multiply(item.getUnitPrice())
                );

        // 과세상품인지 확인
        BigDecimal taxAmount =
                TAXABLE.equals(reference.getTaxType())

                        // 과세상품이면 공급가액의 10%를 계산하고 원 미만 절사
                        ? truncateWon(
                        supplyAmount.multiply(VAT_RATE)
                )

                        // 면세상품이면 세액은 0원
                        : ZERO_MONEY;

        // 총금액 = 공급가액 + 세액
        BigDecimal totalAmount =
                supplyAmount.add(taxAmount);

        // 환산수량이 NUMBER(19,3)에 들어갈 수 있는지 검사
        validateQuantityColumnRange(baseOrderedQty);

        // 공급가액이 NUMBER(19,2)에 들어갈 수 있는지 검사
        validateMoneyColumnRange(supplyAmount);

        // 세액이 NUMBER(19,2)에 들어갈 수 있는지 검사
        validateMoneyColumnRange(taxAmount);

        // 총금액이 NUMBER(19,2)에 들어갈 수 있는지 검사
        validateMoneyColumnRange(totalAmount);

        // INSERT에 전달할 객체를 생성
        PurchaseOrderItemInsertParam itemParam =
                new PurchaseOrderItemInsertParam();

        // 실제 상품 ID를 저장
        itemParam.setProductId(reference.getProductId());

        // 실제 상품단위 ID를 저장
        itemParam.setProductUnitId(reference.getProductUnitId());

        // 사용자가 입력한 거래단위 발주수량을 저장
        itemParam.setOrderedQty(item.getOrderedQty());

        // 발주 당시의 환산수량을 Snapshot으로 저장
        itemParam.setConversionQty(reference.getConversionQty());

        // 계산된 기준단위 발주수량을 저장
        itemParam.setBaseOrderedQty(baseOrderedQty);

        // 사용자가 입력한 매입단가를 저장
        itemParam.setUnitPrice(item.getUnitPrice());

        // 발주 당시 상품 과세유형을 Snapshot으로 저장
        itemParam.setTaxType(reference.getTaxType());

        // Backend에서 계산한 공급가액을 저장
        itemParam.setSupplyAmount(supplyAmount);

        // Backend에서 계산한 세액을 저장
        itemParam.setTaxAmount(taxAmount);

        // Backend에서 계산한 최종금액을 저장
        itemParam.setTotalAmount(totalAmount);

        // 계산이 끝난 INSERT용 객체를 반환
        return itemParam;
    }

    // 발주수량 × 환산수량을 정확하게 소수점 3자리로 표현할 수 있는지 검사
    private BigDecimal calculateExactBaseQty(
            BigDecimal orderedQty,
            BigDecimal conversionQty
    ) {

        // 실제 기준수량을 계산
        BigDecimal calculatedBaseQty =
                orderedQty.multiply(conversionQty);

        try {

            // 자동 반올림이나 절사 없이 정확히 소수 3자리로 표현
            return calculatedBaseQty.setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException exception) {

            // 소수 3자리로 정확하게 표현되지 않으면 등록을 차단
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "환산된 기준수량은 소수점 3자리까지 정확하게 표현되어야 합니다."
            );
        }
    }

    // 금액을 원 단위로 절사
    private BigDecimal truncateWon(
            BigDecimal amount
    ) {

        // 먼저 소수점 이하를 모두 버림
        return amount

                // RoundingMode.DOWN은 여기서는 0 이상의 금액을 아래쪽으로 절사
                .setScale(
                        0,
                        RoundingMode.DOWN
                )

                // DB NUMBER(19,2)에 맞춰 소수 두 자리 형태로 다시 만듦
                .setScale(2);
    }

    // NUMBER(19,3)의 정수부 최대 16자리인지 확인
    private void validateQuantityColumnRange(
            BigDecimal quantity
    ) {

        // 전체 유효숫자 수에서 소수 자릿수를 빼 정수부 자릿수를 계산
        int integerDigits =
                Math.max(
                        0,
                        quantity.precision() - quantity.scale()
                );

        // 정수부가 16자리를 넘으면 NUMBER(19,3)에 저장할 수 없음
        if (integerDigits > 16) {

            // Oracle 오류가 발생하기 전에 업무 예외로 차단
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "환산된 기준수량이 저장 가능한 범위를 초과했습니다."
            );
        }
    }

    // NUMBER(19,2)의 정수부 최대 17자리인지 확인
    private void validateMoneyColumnRange(
            BigDecimal amount
    ) {

        // 금액의 정수부 자릿수를 계산
        int integerDigits =
                Math.max(
                        0,
                        amount.precision() - amount.scale()
                );

        // 정수부가 17자리를 넘으면 NUMBER(19,2)에 저장할 수 없음
        if (integerDigits > 17) {

            // DB INSERT 전에 요청을 차단
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "계산된 금액이 저장 가능한 범위를 초과했습니다."
            );
        }
    }
}