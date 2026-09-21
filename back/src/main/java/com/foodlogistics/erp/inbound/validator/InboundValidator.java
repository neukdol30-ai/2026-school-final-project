package com.foodlogistics.erp.inbound.validator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class InboundValidator {

    // JWT에서 전달받은 회사 ID와 사용자 ID가 정상인지 확인
    public void validateAuthenticatedUser(
            Long companyId,
            Long appUserId
    ) {
        if (companyId == null
                || companyId <= 0
                || appUserId == null
                || appUserId <= 0) {

            throw new BusinessException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "로그인 사용자 정보를 확인할 수 없습니다."
            );
        }
    }

    // 입고 대상 조회에 사용할 발주 ID가 정상적인 숫자인지 확인
    public void validatePurchaseOrderId(
            Long purchaseOrderId
    ) {
        if (purchaseOrderId == null
                || purchaseOrderId <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주 ID는 0보다 큰 값이어야 합니다."
            );
        }
    }
}