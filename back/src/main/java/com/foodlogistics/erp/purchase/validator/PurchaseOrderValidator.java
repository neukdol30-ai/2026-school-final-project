package com.foodlogistics.erp.purchase.validator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateRequest;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderDetailResponse;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderItemDetailResponse;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderUpdateRequest;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemReference;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PurchaseOrderValidator {

    // PRODUCT.tax_type에서 사용하는 과세 코드
    private static final String TAXABLE = "TAXABLE";

    // PRODUCT.tax_type에서 사용하는 면세 코드
    private static final String TAX_FREE = "TAX_FREE";

    // 발주 수정이 가능한 작성중 상태 코드
    private static final String APPROVAL_STATUS_DRAFT = "DRAFT";

    // 반려된 뒤 내용을 수정할 수 있도록 반려 상태 코드도 별도로 관리
    private static final String APPROVAL_STATUS_REJECTED = "REJECTED";

    // 발주 수정이 가능한 입고상태
    private static final String RECEIPT_STATUS_NOT_RECEIVED = "NOT_RECEIVED";

    // 공급업체와 창고를 실제 DB에서 확인하기 위해 사용
    private final PurchaseOrderMapper purchaseOrderMapper;

    // 프로젝트의 날짜 업무 규칙에서 사용할 한국 표준시간
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    // PURCHASE_ORDER.approval_status에서 DB가 허용하는 승인상태
    private static final Set<String> APPROVAL_STATUSES =
            Set.of(
                    APPROVAL_STATUS_DRAFT,
                    "PENDING",
                    "APPROVED",
                    APPROVAL_STATUS_REJECTED
            );

    // PURCHASE_ORDER.receipt_status에서 DB가 허용하는 입고진행상태
    private static final Set<String> RECEIPT_STATUSES =
            Set.of(
                    RECEIPT_STATUS_NOT_RECEIVED,
                    "PARTIAL",
                    "RECEIVED",
                    "CLOSED"
            );

    // JWT에서 전달받은 회사 ID와 사용자 ID가 정상인지 확인
    public void validateAuthenticatedUser(
            Long companyId,
            Long appUserId
    ) {

        // 회사 ID 또는 사용자 ID가 없거나 0 이하이면 정상 인증정보가 아님
        if (companyId == null
                || companyId <= 0
                || appUserId == null
                || appUserId <= 0) {

            // 인증정보 오류로 처리
            throw new BusinessException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "로그인 사용자 정보를 확인할 수 없습니다."
            );
        }
    }

    // 상세조회에 사용할 발주 ID가 정상적인 숫자인지 확인
    public void validatePurchaseOrderId(
            Long purchaseOrderId
    ) {
        // PK는 null이거나 0 이하일 수 없음
        if (purchaseOrderId == null || purchaseOrderId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주 ID는 0보다 큰 값이어야 합니다."
            );
        }
    }

    // 선택한 거래처가 현재 회사에서 사용할 수 있는 공급업체인지 검사
    public void validateSupplier(
            Long companyId,
            Long supplierId
    ) {

        // DB 조회 결과가 정확히 1건인지 확인
        if (purchaseOrderMapper.countAvailableSupplier(
                companyId,
                supplierId
        ) != 1) {

            // 다른 회사 공급업체이거나 사용중지 거래처이면 등록을 막음
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "현재 회사에서 사용할 수 있는 공급업체가 아닙니다."
            );
        }
    }

    // 선택한 창고가 현재 회사에서 사용할 수 있는 창고인지 검사
    public void validateWarehouse(
            Long companyId,
            Long warehouseId
    ) {

        // DB 조회 결과가 정확히 1건인지 확인
        if (purchaseOrderMapper.countAvailableWarehouse(
                companyId,
                warehouseId
        ) != 1) {

            // 다른 회사 창고이거나 사용중지 창고이면 등록을 막음
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "현재 회사에서 사용할 수 있는 창고가 아닙니다."
            );
        }
    }

    // 신규 발주 등록 시 날짜 업무 규칙을 검사
    public void validateDatesForCreate(
            PurchaseOrderCreateRequest request
    ) {

        // React에서 받은 발주일을 꺼냄
        LocalDate orderDate = request.getOrderDate();

        LocalDate expectedDeliveryDate = request.getExpectedDeliveryDate();

        // 신규 등록용 발주일 범위를 검사
        validateOrderDateForCreate(orderDate);

        validateExpectedDeliveryDate(orderDate, expectedDeliveryDate);
    }

    // 기존 발주 수정 시 날짜 업무 규칙을 검사
    public void validateDatesForUpdate(
            LocalDate existingOrderDate,
            PurchaseOrderUpdateRequest request
    ) {

        // DB에 기존부터 저장되어 있던 발주일
        if (existingOrderDate == null) {
            throw new IllegalStateException(
                    "기존 발주일을 확인할 수 없습니다."
            );
        }

        // 수정 요청으로 들어온 새로운 발주일
        LocalDate requestedOrderDate = request.getOrderDate();

        LocalDate expectedDeliveryDate = request.getExpectedDeliveryDate();

        validateOrderDateForUpdate(existingOrderDate, requestedOrderDate);

        validateExpectedDeliveryDate(requestedOrderDate, expectedDeliveryDate);
    }

    // 신규 등록 시 발주일 허용 범위를 검사
    private void validateOrderDateForCreate(
            LocalDate orderDate
    ) {
        // DTO의 @NotNull에서도 검사하지만, Validator 내부에서도 잘못된 호출을 방어
        if (orderDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주일을 입력해 주십시오."
            );
        }

        // 서버 기본 시간대가 아니라 한국시간 기준 오늘 날짜를 구함
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        // 발주일의 가장 오래된 허용 날짜
        // 예: 오늘 2026-09-16 -> 2026-08-16
        LocalDate minimumOrderDate = today.minusMonths(1);

        // 오늘보다 1개월보다 더 과거이면 신규 등록 차단
        if (orderDate.isBefore(minimumOrderDate)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주일은 한국시간 기준 최근 1개월 이내의 날짜만 입력할 수 있습니다."
            );
        }

        // 아직 오지 않은 미래 날짜의 발주는 차단
        if (orderDate.isAfter(today)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주일은 한국시간 기준 오늘보다 미래로 지정할 수 없습니다."
            );
        }
    }

    // 기존 발주 수정 시 발주일이 변경되지 않았는지 검사
    private void validateOrderDateForUpdate(
            LocalDate existingOrderDate,
            LocalDate requestedOrderDate
    ) {
        if (requestedOrderDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주일을 입력해 주십시오."
            );
        }

        if (!existingOrderDate.equals(requestedOrderDate)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "등록된 발주의 발주일은 변경할 수 없습니다."
            );
        }
    }

    // 발주일과 납품희망일의 관계를 검사
    private void validateExpectedDeliveryDate(
            LocalDate orderDate,
            LocalDate expectedDeliveryDate
    ) {

        // 납품희망일은 선택값이므로 입력하지 않았다면 검사 종료
        if (expectedDeliveryDate == null) {
            return;
        }

        // 발주보다 먼저 납품받는 날짜는 업무상 허용하지 않음
        if (expectedDeliveryDate.isBefore(orderDate)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "납품희망일은 발주일보다 빠를 수 없습니다."
            );
        }

        // Java LocalDate 기준 발주일에서 정확히 1개월 뒤 날짜를 계산
        LocalDate maximumExpectedDeliveryDate = orderDate.plusMonths(1);

        // 발주일에서 1개월보다 더 뒤의 납품희망일은 차단
        if (expectedDeliveryDate.isAfter(
                maximumExpectedDeliveryDate
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "납품희망일은 발주일로부터 1개월을 초과할 수 없습니다."
            );
        }
    }

    // 기존 발주가 수정 가능한 상태인지 검사
    public void validateUpdatablePurchaseOrder(
            PurchaseOrderDetailResponse purchaseOrder,
            List<PurchaseOrderItemDetailResponse> items
    ) {

        // 현재 발주의 승인상태를 변수에 보관
        String approvalStatus = purchaseOrder.getApprovalStatus();

        // DRAFT(작성중)도 아니고 REJECTED(반려)도 아니면 수정 차단
        if (!APPROVAL_STATUS_DRAFT.equals(approvalStatus)
                && !APPROVAL_STATUS_REJECTED.equals(approvalStatus)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "DRAFT(작성중) 또는 REJECTED(반려) 상태의 발주만 수정할 수 있습니다."
            );
        }

        if (!RECEIPT_STATUS_NOT_RECEIVED.equals(
                purchaseOrder.getReceiptStatus()
        )) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입고가 시작된 발주는 수정할 수 없습니다."
            );
        }

        for (PurchaseOrderItemDetailResponse item : items) {

            BigDecimal receivedQty =
                    item.getReceivedQty();

            if (receivedQty == null) {
                throw new IllegalStateException(
                        "발주 품목의 입고수량을 확인할 수 없습니다."
                );
            }

            if (receivedQty.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "이미 입고된 품목이 있는 발주는 수정할 수 없습니다."
                );
            }
        }
    }

    // DB에서 조회한 상품/상품단위 기준정보가 정상인지 검사
    public void validateReferenceData(
            PurchaseOrderItemReference reference
    ) {

        // 환산수량이 없거나 0 이하이면 잘못된 기준정보
        if (reference.getConversionQty() == null
                || reference.getConversionQty()
                .compareTo(BigDecimal.ZERO) <= 0) {

            // DB 기준정보 자체의 이상이므로 서버 내부 문제로 처리
            throw new IllegalStateException(
                    "상품단위의 환산수량이 올바르지 않습니다."
            );
        }

        // TAXABLE과 TAX_FREE 둘 중 하나인지 확인
        if (!TAXABLE.equals(reference.getTaxType())
                && !TAX_FREE.equals(reference.getTaxType())) {

            // DB 기준정보 자체의 이상임
            throw new IllegalStateException(
                    "상품의 과세유형이 올바르지 않습니다."
            );
        }
    }

    // 발주 목록 검색조건이 정상인지 검사
    public void validateListSearchConditions(
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            String approvalStatus,
            String receiptStatus
    ) {

        // 시작일과 종료일이 모두 있다면 시작일이 더 늦을 수 없음
        if (orderDateFrom != null
                && orderDateTo != null
                && orderDateFrom.isAfter(orderDateTo)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주일 조회 시작일은 종료일보다 늦을 수 없습니다."
            );
        }

        // 승인상태가 들어왔다면 DB에 정의된 상태만 허용
        if (approvalStatus != null
                && !APPROVAL_STATUSES.contains(approvalStatus)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "올바르지 않은 발주 승인상태입니다."
            );
        }

        // 입고상태가 들어왔다면 DB에 정의된 상태만 허용
        if (receiptStatus != null
                && !RECEIPT_STATUSES.contains(receiptStatus)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "올바르지 않은 발주 입고상태입니다."
            );
        }
    }
}