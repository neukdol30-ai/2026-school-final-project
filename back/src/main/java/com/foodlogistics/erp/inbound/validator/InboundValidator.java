package com.foodlogistics.erp.inbound.validator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.mapper.InboundPurchaseOrderLockInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class InboundValidator {

    private static final ZoneId KST =
            ZoneId.of("Asia/Seoul");

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

    // 발주 ID 기본 검증
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

    // 잠근 발주가 실제 입고 가능한 상태인지 검증
    public void validatePurchaseOrderForInbound(
            InboundPurchaseOrderLockInfo purchaseOrder
    ) {
        if (purchaseOrder == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "입고 대상 발주를 찾을 수 없습니다."
            );
        }

        if (!"APPROVED".equals(
                purchaseOrder.getApprovalStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "APPROVED(승인완료) 상태의 발주만 입고할 수 있습니다."
            );
        }

        String receiptStatus =
                purchaseOrder.getReceiptStatus();

        if (!"NOT_RECEIVED".equals(receiptStatus)
                && !"PARTIAL".equals(receiptStatus)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "NOT_RECEIVED(미입고) 또는 PARTIAL(부분입고) 상태의 발주만 입고할 수 있습니다."
            );
        }
    }

    // 아직 입고 잔량이 남아 있는지 검증
    public void validateRemainingPurchaseOrderItems(
            int remainingItemCount
    ) {
        if (remainingItemCount <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "해당 발주에는 남은 입고 수량이 없습니다."
            );
        }
    }

    // 입고일 업무 규칙 검증
    public void validateInboundDate(
            LocalDate inboundDate,
            LocalDate orderDate,
            LocalDate latestConfirmedInboundDate
    ) {
        if (inboundDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고일을 입력해 주십시오."
            );
        }

        if (orderDate == null) {
            throw new IllegalStateException(
                    "발주일 정보를 확인할 수 없습니다."
            );
        }

        LocalDate today =
                LocalDate.now(KST);

        if (inboundDate.isBefore(orderDate)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고일은 발주일보다 빠를 수 없습니다."
            );
        }

        if (latestConfirmedInboundDate != null
                && inboundDate.isBefore(
                latestConfirmedInboundDate
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고일은 가장 최근 확정 입고일보다 빠를 수 없습니다."
            );
        }

        if (inboundDate.isAfter(today)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고일은 오늘 이후 날짜로 입력할 수 없습니다."
            );
        }
    }

    // 동일 발주의 기존 DRAFT 존재 여부 검증
    public void validateNoExistingDraft(
            int draftCount
    ) {
        if (draftCount > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "해당 발주에는 이미 DRAFT(작성중) 입고서가 존재합니다."
            );
        }
    }
}