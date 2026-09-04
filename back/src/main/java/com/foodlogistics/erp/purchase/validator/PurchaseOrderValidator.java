package com.foodlogistics.erp.purchase.validator;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateRequest;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemReference;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PurchaseOrderValidator {

    // PRODUCT.tax_type에서 사용하는 과세 코드
    private static final String TAXABLE = "TAXABLE";

    // PRODUCT.tax_type에서 사용하는 면세 코드
    private static final String TAX_FREE = "TAX_FREE";

    // 공급업체와 창고를 실제 DB에서 확인하기 위해 사용
    private final PurchaseOrderMapper purchaseOrderMapper;

    // PURCHASE_ORDER.approval_status에서 DB가 허용하는 승인상태
    private static final Set<String> APPROVAL_STATUSES =
            Set.of(
                    "DRAFT",
                    "PENDING",
                    "APPROVED",
                    "REJECTED"
            );

    // PURCHASE_ORDER.receipt_status에서 DB가 허용하는 입고진행상태
    private static final Set<String> RECEIPT_STATUSES =
            Set.of(
                    "NOT_RECEIVED",
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

    // 발주일과 납품희망일의 날짜 관계를 검사
    public void validateDates(
            PurchaseOrderCreateRequest request
    ) {

        // 납품희망일이 입력된 경우에만 검사
        if (request.getExpectedDeliveryDate() != null
                && request.getExpectedDeliveryDate()
                .isBefore(request.getOrderDate())) {

            // 납품희망일이 발주일보다 과거이면 등록할 수 없음
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "납품희망일은 발주일보다 빠를 수 없습니다."
            );
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
    public void validateListSearchConditions (
            Long supplierId,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            String approvalStatus,
            String receiptStatus
    ) {
        //  공급업체 필터가 들어왔다면 PK는 0보다 커야 함
        if (supplierId != null && supplierId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "공급업체 ID는 0보다 커야 합니다."
            );
        }

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