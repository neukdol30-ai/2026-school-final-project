package com.foodlogistics.erp.inbound.validator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.dto.InboundItemLotRequest;
import com.foodlogistics.erp.inbound.dto.InboundItemUpdateRequest;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.mapper.InboundConfirmItemInfo;
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

    // 입고서 존재 여부 검증
    public void validateInboundExists(
            InboundItemUpdateTargetInfo inbound
    ) {
        if (inbound == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "입고서를 찾을 수 없습니다."
            );
        }
    }

    // 입고서가 DRAFT 상태인지 검증
    public void validateInboundDraft(
            InboundItemUpdateTargetInfo inbound
    ) {
        if (inbound == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "입고서를 찾을 수 없습니다."
            );
        }

        if (!"DRAFT".equals(inbound.getStatus())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "DRAFT(작성중) 상태의 입고서만 수정할 수 있습니다."
            );
        }
    }

    // DRAFT 품목 요청 전체 기본 검증
    public void validateItemRequests(
            List<InboundItemUpdateRequest> items
    ) {
        if (items == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고품목 목록을 확인해 주십시오."
            );
        }

        // 빈 목록은 DRAFT 품목 전체 삭제 의미로 허용
        if (items.isEmpty()) {
            return;
        }

        Set<Long> purchaseOrderItemIds =
                new HashSet<>();

        for (InboundItemUpdateRequest item : items) {

            if (item == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "입고품목 정보를 확인해 주십시오."
                );
            }

            Long purchaseOrderItemId =
                    item.getPurchaseOrderItemId();

            if (purchaseOrderItemId == null
                    || purchaseOrderItemId <= 0) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "발주품목 ID는 0보다 큰 값이어야 합니다."
                );
            }

            if (!purchaseOrderItemIds.add(
                    purchaseOrderItemId
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "동일한 발주 품목을 중복 입력할 수 없습니다."
                );
            }

            validateDatabaseQuantity(
                    item.getReceivedQty()
            );

            validateLotRequestBasics(
                    item.getLots()
            );
        }
    }

    // 요청한 발주품목이 현재 입고서에서 처리 가능한지 검증
    public void validateInboundPurchaseOrderItem(
            InboundPurchaseOrderItemResponse item
    ) {
        if (item == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "현재 입고서에서 처리할 수 없는 발주 품목입니다."
            );
        }

        if (item.getProductId() == null
                || item.getProductId() <= 0
                || item.getProductUnitId() == null
                || item.getProductUnitId() <= 0) {

            throw new IllegalStateException(
                    "발주 품목의 상품 또는 단위 정보를 확인할 수 없습니다."
            );
        }

        validateDatabaseQuantity(
                item.getConversionQty()
        );

        if (item.getRemainingBaseQty() == null
                || item.getRemainingBaseQty()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "해당 발주 품목에는 남은 입고 수량이 없습니다."
            );
        }

        String lotManagedYn =
                item.getLotManagedYn();

        if (!"Y".equals(lotManagedYn)
                && !"N".equals(lotManagedYn)) {

            throw new IllegalStateException(
                    "상품의 LOT 관리 여부를 확인할 수 없습니다."
            );
        }
    }

    // 기준입고수량이 DB 저장범위 안이고 발주 잔량을 넘지 않는지 검증
    public void validateBaseReceivedQty(
            BigDecimal baseReceivedQty,
            BigDecimal remainingBaseQty
    ) {
        validateDatabaseQuantity(
                baseReceivedQty
        );

        if (remainingBaseQty == null
                || remainingBaseQty
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "해당 발주 품목에는 남은 입고 수량이 없습니다."
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

    // LOT 관리 여부에 따라 LOT 요청을 검증
    public void validateLotPolicy(
            String lotManagedYn,
            List<InboundItemLotRequest> lots,
            BigDecimal baseReceivedQty
    ) {
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

        if (!"Y".equals(lotManagedYn)) {
            throw new IllegalStateException(
                    "상품의 LOT 관리 여부를 확인할 수 없습니다."
            );
        }

        if (lots == null
                || lots.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT 관리상품에는 LOT 정보를 입력해야 합니다."
            );
        }

        Set<String> lotNos =
                new HashSet<>();

        BigDecimal totalBaseLotQty =
                BigDecimal.ZERO;

        for (InboundItemLotRequest lot : lots) {

            if (lot == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 정보를 확인해 주십시오."
                );
            }

            String lotNo =
                    lot.getLotNo();

            if (lotNo == null
                    || lotNo.trim().isEmpty()) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 번호를 입력해 주십시오."
                );
            }

            String normalizedLotNo =
                    lotNo.trim();

            if (!lotNos.add(
                    normalizedLotNo
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "동일한 LOT 번호를 한 품목에 중복 입력할 수 없습니다."
                );
            }

            validateDatabaseQuantity(
                    lot.getBaseLotQty()
            );

            validateLotDates(
                    lot.getManufactureDate(),
                    lot.getExpiryDate()
            );

            totalBaseLotQty =
                    totalBaseLotQty.add(
                            lot.getBaseLotQty()
                    );
        }

        if (totalBaseLotQty.compareTo(
                baseReceivedQty
        ) != 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT별 기준수량 합계는 품목의 기준입고수량과 같아야 합니다."
            );
        }
    }

    // 기존 LOT와 요청 LOT의 날짜가 충돌하는지 검증
    public void validateExistingLotDates(
            LotInfo existingLot,
            InboundItemLotRequest requestLot
    ) {
        if (existingLot == null
                || requestLot == null) {
            return;
        }

        if (existingLot.getManufactureDate() != null
                && requestLot.getManufactureDate() != null
                && !existingLot.getManufactureDate().equals(
                requestLot.getManufactureDate()
        )) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "기존 LOT의 제조일과 입력한 제조일이 일치하지 않습니다."
            );
        }

        if (existingLot.getExpiryDate() != null
                && requestLot.getExpiryDate() != null
                && !existingLot.getExpiryDate().equals(
                requestLot.getExpiryDate()
        )) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "기존 LOT의 유통기한과 입력한 유통기한이 일치하지 않습니다."
            );
        }
    }

    // 입고확정 대상 품목과 LOT 상태 재검증
    public void validateInboundConfirmItems(
            List<InboundConfirmItemInfo> items
    ) {
        if (items == null
                || items.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고확정할 품목이 없습니다."
            );
        }

        for (InboundConfirmItemInfo item : items) {

            if (item == null
                    || item.getInboundItemId() == null
                    || item.getInboundItemId() <= 0
                    || item.getPurchaseOrderItemId() == null
                    || item.getPurchaseOrderItemId() <= 0
                    || item.getProductId() == null
                    || item.getProductId() <= 0) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "입고확정 품목 정보를 확인할 수 없습니다."
                );
            }

            validateDatabaseQuantity(
                    item.getBaseReceivedQty()
            );

            if ("Y".equals(
                    item.getLotManagedYn()
            )) {

                if (item.getLotCount() <= 0) {
                    throw new BusinessException(
                            ErrorCode.INVALID_REQUEST,
                            "LOT 관리상품에는 LOT 정보가 필요합니다."
                    );
                }

                if (item.getBaseLotQtyTotal() == null) {
                    throw new BusinessException(
                            ErrorCode.INVALID_REQUEST,
                            "LOT 수량 정보를 확인할 수 없습니다."
                    );
                }

                if (item.getBaseLotQtyTotal()
                        .compareTo(
                                item.getBaseReceivedQty()
                        ) != 0) {

                    throw new BusinessException(
                            ErrorCode.INVALID_REQUEST,
                            "LOT별 기준수량 합계는 품목의 기준입고수량과 같아야 합니다."
                    );
                }

                continue;
            }

            if ("N".equals(
                    item.getLotManagedYn()
            )) {

                if (item.getLotCount() != 0) {
                    throw new BusinessException(
                            ErrorCode.INVALID_REQUEST,
                            "LOT 비관리상품에는 LOT 정보를 저장할 수 없습니다."
                    );
                }

                continue;
            }

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "상품의 LOT 관리 여부가 올바르지 않습니다."
            );
        }
    }

    // 발주품목 조건부 UPDATE 결과 검증
    public void validatePurchaseOrderItemUpdateCount(
            int updatedCount
    ) {
        if (updatedCount != 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고수량이 발주 잔량을 초과하거나 처리할 수 없는 발주 품목입니다."
            );
        }
    }

    // 발주 Header 입고상태 UPDATE 결과 검증
    public void validatePurchaseOrderStatusUpdateCount(
            int updatedCount
    ) {
        if (updatedCount != 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주의 입고상태를 변경할 수 없습니다."
            );
        }
    }

    // INBOUND 확정 UPDATE 결과 검증
    public void validateInboundConfirmUpdateCount(
            int updatedCount
    ) {
        if (updatedCount != 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "이미 확정되었거나 확정할 수 없는 입고서입니다."
            );
        }
    }

    // LOT 제조일과 유통기한 순서 검증
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

    // LOT 요청의 기본값 검증
    private void validateLotRequestBasics(
            List<InboundItemLotRequest> lots
    ) {
        if (lots == null
                || lots.isEmpty()) {
            return;
        }

        for (InboundItemLotRequest lot : lots) {

            if (lot == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 정보를 확인해 주십시오."
                );
            }

            String lotNo =
                    lot.getLotNo();

            if (lotNo == null
                    || lotNo.trim().isEmpty()) {

                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 번호를 입력해 주십시오."
                );
            }

            validateDatabaseQuantity(
                    lot.getBaseLotQty()
            );

            validateLotDates(
                    lot.getManufactureDate(),
                    lot.getExpiryDate()
            );
        }
    }

    // Oracle NUMBER(19,3)에 정확히 저장할 수 있는 양수인지 검증
    private void validateDatabaseQuantity(
            BigDecimal quantity
    ) {
        if (quantity == null
                || quantity.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "수량은 0보다 커야 합니다."
            );
        }

        BigDecimal scaledQuantity;

        try {
            scaledQuantity =
                    quantity.setScale(
                            3,
                            RoundingMode.UNNECESSARY
                    );
        } catch (ArithmeticException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "수량은 소수점 셋째 자리까지만 입력할 수 있습니다."
            );
        }

        if (scaledQuantity.precision() > 19) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "수량이 저장 가능한 범위를 초과했습니다."
            );
        }
    }
}