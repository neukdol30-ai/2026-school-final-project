package com.foodlogistics.erp.inbound.validator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.dto.InboundItemLotRequest;
import com.foodlogistics.erp.inbound.dto.InboundItemUpdateRequest;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.mapper.InboundItemUpdateTargetInfo;
import com.foodlogistics.erp.inbound.mapper.InboundPurchaseOrderLockInfo;
import com.foodlogistics.erp.inbound.mapper.LotInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // 입고 ID 기본 검증
    public void validateInboundId(
            Long inboundId
    ) {
        if (inboundId == null
                || inboundId <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고 ID는 0보다 큰 값이어야 합니다."
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

    // 입고품목을 수정할 INBOUND가 존재하는지 확인
    public void validateInboundExists(
            InboundItemUpdateTargetInfo inbound
    ) {
        if (inbound == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "입고 정보를 찾을 수 없습니다."
            );
        }
    }

    // 입고품목 수정은 DRAFT(작성중) 상태에서만 허용
    public void validateInboundDraft(
            InboundItemUpdateTargetInfo inbound
    ) {
        validateInboundExists(
                inbound
        );

        if (!"DRAFT".equals(
                inbound.getStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "DRAFT(작성중) 상태의 입고서만 품목을 수정할 수 있습니다."
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

    // 전체 입고품목 요청의 기본값과 중복을 검증
    public void validateItemRequests(
            List<InboundItemUpdateRequest> items
    ) {
        if (items == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고 품목 목록을 확인할 수 없습니다."
            );
        }

        // 빈 목록 []은 DRAFT의 품목 전체 삭제 의미이므로 허용
        Set<Long> purchaseOrderItemIds =
                new HashSet<>();

        for (InboundItemUpdateRequest item : items) {

            if (item == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "입고 품목 정보가 올바르지 않습니다."
                );
            }

            Long purchaseOrderItemId =
                    item.getPurchaseOrderItemId();

            if (purchaseOrderItemId == null
                    || purchaseOrderItemId <= 0) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "발주 품목 ID는 0보다 큰 값이어야 합니다."
                );
            }

            // 같은 발주품목을 요청 안에 두 번 넣는 것을 차단
            if (!purchaseOrderItemIds.add(
                    purchaseOrderItemId
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "동일한 발주 품목을 중복 입력할 수 없습니다."
                );
            }

            validateDatabaseQuantity(
                    item.getReceivedQty(),
                    "입고수량"
            );

            validateLotRequestBasics(
                    item.getLots()
            );
        }
    }

    // 요청한 발주품목이 현재 입고서의 발주에 실제로 존재하는지 검증
    public void validateInboundPurchaseOrderItem(
            InboundPurchaseOrderItemResponse purchaseOrderItem
    ) {
        if (purchaseOrderItem == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "현재 입고서에서 처리할 수 없는 발주 품목입니다."
            );
        }

        if (purchaseOrderItem.getProductId() == null
                || purchaseOrderItem.getProductUnitId() == null) {

            throw new IllegalStateException(
                    "발주 품목의 상품 또는 상품단위 정보를 확인할 수 없습니다."
            );
        }

        BigDecimal conversionQty =
                purchaseOrderItem.getConversionQty();

        if (conversionQty == null
                || conversionQty.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalStateException(
                    "발주 품목의 환산수량을 확인할 수 없습니다."
            );
        }

        BigDecimal remainingBaseQty =
                purchaseOrderItem.getRemainingBaseQty();

        if (remainingBaseQty == null
                || remainingBaseQty.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "해당 발주 품목에는 남은 입고 수량이 없습니다."
            );
        }

        String lotManagedYn =
                purchaseOrderItem.getLotManagedYn();

        if (!"Y".equals(lotManagedYn)
                && !"N".equals(lotManagedYn)) {

            throw new IllegalStateException(
                    "상품의 LOT 관리 여부를 확인할 수 없습니다."
            );
        }
    }

    // 계산된 기준입고수량이 남은 발주수량을 초과하지 않는지 검증
    public void validateBaseReceivedQty(
            BigDecimal baseReceivedQty,
            BigDecimal remainingBaseQty
    ) {
        validateDatabaseQuantity(
                baseReceivedQty,
                "기준입고수량"
        );

        if (remainingBaseQty == null) {
            throw new IllegalStateException(
                    "남은 발주수량을 확인할 수 없습니다."
            );
        }

        if (baseReceivedQty.compareTo(
                remainingBaseQty
        ) > 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고수량이 남은 발주수량을 초과할 수 없습니다."
            );
        }
    }

    // 상품의 LOT 관리 여부와 요청 LOT 목록을 함께 검증
    public void validateLotPolicy(
            String lotManagedYn,
            List<InboundItemLotRequest> lots,
            BigDecimal baseReceivedQty
    ) {
        // LOT 비관리상품은 LOT 입력 자체를 허용하지 않음
        if ("N".equals(lotManagedYn)) {

            if (lots != null
                    && !lots.isEmpty()) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 비관리상품에는 LOT 정보를 입력할 수 없습니다."
                );
            }

            return;
        }

        // 여기부터는 LOT 관리상품
        if (!"Y".equals(lotManagedYn)) {
            throw new IllegalStateException(
                    "상품의 LOT 관리 여부를 확인할 수 없습니다."
            );
        }

        if (lots == null
                || lots.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT 관리상품은 LOT 정보를 1건 이상 입력해야 합니다."
            );
        }

        Set<String> lotNos =
                new HashSet<>();

        BigDecimal lotQtyTotal =
                BigDecimal.ZERO;

        for (InboundItemLotRequest lot : lots) {

            if (lot == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 정보가 올바르지 않습니다."
                );
            }

            String lotNo =
                    lot.getLotNo();

            if (lotNo == null
                    || lotNo.isBlank()) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 번호를 입력해 주십시오."
                );
            }

            String normalizedLotNo =
                    lotNo.trim();

            if (normalizedLotNo.length() > 50) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 번호는 50자 이하여야 합니다."
                );
            }

            // 같은 품목 안에서 동일 LOT 번호 중복 입력 차단
            if (!lotNos.add(
                    normalizedLotNo
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "동일한 LOT 번호를 한 품목에 중복 입력할 수 없습니다."
                );
            }

            validateDatabaseQuantity(
                    lot.getBaseLotQty(),
                    "LOT 기준수량"
            );

            validateLotDates(
                    lot.getManufactureDate(),
                    lot.getExpiryDate()
            );

            lotQtyTotal =
                    lotQtyTotal.add(
                            lot.getBaseLotQty()
                    );
        }

        // LOT별 기준수량 합계는 품목의 기준입고수량과 정확히 같아야 함
        if (lotQtyTotal.compareTo(
                baseReceivedQty
        ) != 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT별 기준수량 합계는 품목의 기준입고수량과 같아야 합니다."
            );
        }
    }

    // 기존 LOT를 재사용할 때 날짜가 서로 충돌하는지 검증
    public void validateExistingLotDates(
            LotInfo existingLot,
            InboundItemLotRequest requestLot
    ) {
        if (existingLot == null
                || requestLot == null) {

            return;
        }

        LocalDate existingManufactureDate =
                existingLot.getManufactureDate();

        LocalDate requestedManufactureDate =
                requestLot.getManufactureDate();

        if (existingManufactureDate != null
                && requestedManufactureDate != null
                && !existingManufactureDate.equals(
                requestedManufactureDate
        )) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "기존 LOT의 제조일과 요청한 제조일이 일치하지 않습니다."
            );
        }

        LocalDate existingExpiryDate =
                existingLot.getExpiryDate();

        LocalDate requestedExpiryDate =
                requestLot.getExpiryDate();

        if (existingExpiryDate != null
                && requestedExpiryDate != null
                && !existingExpiryDate.equals(
                requestedExpiryDate
        )) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "기존 LOT의 유통기한과 요청한 유통기한이 일치하지 않습니다."
            );
        }
    }

    // LOT 제조일과 유통기한의 순서를 검증
    private void validateLotDates(
            LocalDate manufactureDate,
            LocalDate expiryDate
    ) {
        if (manufactureDate != null
                && expiryDate != null
                && manufactureDate.isAfter(
                expiryDate
        )) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT 제조일은 유통기한보다 늦을 수 없습니다."
            );
        }
    }

    // LOT 목록의 DTO 기본값을 한번 더 방어
    private void validateLotRequestBasics(
            List<InboundItemLotRequest> lots
    ) {
        if (lots == null) {
            return;
        }

        for (InboundItemLotRequest lot : lots) {

            if (lot == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 정보가 올바르지 않습니다."
                );
            }

            String lotNo =
                    lot.getLotNo();

            if (lotNo == null
                    || lotNo.isBlank()) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 번호를 입력해 주십시오."
                );
            }

            if (lotNo.trim().length() > 50) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 번호는 50자 이하여야 합니다."
                );
            }

            validateDatabaseQuantity(
                    lot.getBaseLotQty(),
                    "LOT 기준수량"
            );

            validateLotDates(
                    lot.getManufactureDate(),
                    lot.getExpiryDate()
            );
        }
    }

    // Oracle NUMBER(19,3)에 저장할 수량을 Backend에서 먼저 정확하게 검증
    private void validateDatabaseQuantity(
            BigDecimal quantity,
            String fieldName
    ) {
        if (quantity == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    fieldName + "을(를) 입력해 주십시오."
            );
        }

        if (quantity.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    fieldName + "은(는) 0보다 커야 합니다."
            );
        }

        BigDecimal scaledQuantity;

        try {
            // 반올림하지 않고 정확히 소수 3자리로 표현 가능한지 확인
            scaledQuantity =
                    quantity.setScale(
                            3,
                            RoundingMode.UNNECESSARY
                    );
        } catch (ArithmeticException e) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    fieldName + "은(는) 소수 3자리까지만 입력할 수 있습니다."
            );
        }

        // NUMBER(19,3)의 전체 유효숫자 19자리를 초과하지 않도록 방어
        if (scaledQuantity.precision() > 19) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    fieldName + "의 숫자 자릿수가 너무 큽니다."
            );
        }
    }
}