package com.foodlogistics.erp.purchase.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.purchase.calculator.PurchaseOrderCalculator;
import com.foodlogistics.erp.purchase.dto.*;
import com.foodlogistics.erp.purchase.entity.PurchaseOrder;
import com.foodlogistics.erp.purchase.entity.PurchaseOrderApprovalStatus;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemReference;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderMapper;
// Service에서 검증·계산한 발주 수정값을 PurchaseOrderMapper.xml의 UPDATE SQL까지 전달하는 내부 객체
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderUpdateParam;
import com.foodlogistics.erp.purchase.repository.PurchaseOrderRepository;
import com.foodlogistics.erp.purchase.validator.PurchaseOrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    // 신규 발주 등록 시 사용하는 발주 승인상태
    private static final String APPROVAL_STATUS_DRAFT = "DRAFT";

    // 신규 발주는 아직 한 번도 입고되지 않았으므로 미입고 상태로 반환
    private static final String RECEIPT_STATUS_NOT_RECEIVED = "NOT_RECEIVED";

    // 발주번호의 날짜 부분을 yyyyMMdd 형태로 만들기 위한 Formatter
    // 예: 2026-09-03 -> 20260903
    private static final DateTimeFormatter ORDER_NO_DATE_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE;

    // MyBatis를 통해 발주 Header와 Item을 저장하고 기준정보를 조회
    private final PurchaseOrderMapper purchaseOrderMapper;

    // 승인 요청처럼 단일 발주의 상태를 변경할 때 사용할 JPA repository
    // @RequiredArgsConstructor가 이 final 필드를 생성자로 자동 주입합니다.
    private final PurchaseOrderRepository purchaseOrderRepository;

    // 인증정보, 공급업체, 창고, 날짜, DB 기준정보를 검증
    private final PurchaseOrderValidator purchaseOrderValidator;

    // 품목별 기준수량과 공급가액, 세액, 총금액을 계산
    private final PurchaseOrderCalculator purchaseOrderCalculator;

    // 발주서 한 건을 등록하는 핵심 Service 메서드
    @Transactional
    public PurchaseOrderCreateResponse createPurchaseOrder(
            Long companyId,
            Long appUserId,
            PurchaseOrderCreateRequest request
    ) {

        // 1단계:
        // Controller가 JWT에서 꺼내 전달한 회사 ID와 로그인 사용자 ID를 검증
        purchaseOrderValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        // 2단계:
        // React에서 선택한 공급업체가 현재 로그인 회사에서 사용 가능한지 DB로 확인
        purchaseOrderValidator.validateSupplier(
                companyId,
                request.getSupplierId()
        );

        // 3단계:
        // React에서 선택한 예정 입고창고가 현재 회사에서 사용 가능한지 DB로 확인
        purchaseOrderValidator.validateWarehouse(
                companyId,
                request.getWarehouseId()
        );

        // 4단계:
        // 납품희망일이 발주일보다 과거인지 검사
        purchaseOrderValidator.validateDates(request);

        // 5단계:
        // 각 품목의 검증과 계산이 끝난 INSERT용 객체를 보관할 List
        List<PurchaseOrderItemInsertParam> itemParams =
                validateAndCalculateItems(
                        companyId,
                        request.getItems()
                );

        // 6단계:
        // Header에 저장할 공급가액 합계를 0원부터 시작
        BigDecimal totalSupplyAmount =
                BigDecimal.ZERO.setScale(2);

        // Header에 저장할 세액 합계를 0원부터 시작
        BigDecimal totalTaxAmount =
                BigDecimal.ZERO.setScale(2);

        // Header에 저장할 최종금액 합계를 0원부터 시작
        BigDecimal totalAmount =
                BigDecimal.ZERO.setScale(2);

        // Calculator가 계산해 둔 각 품목의 금액을 Header 합계로 더함
        for (PurchaseOrderItemInsertParam itemParam : itemParams) {

            // 전체 공급가액 = 기존 합계 + 현재 품목 공급가액
            totalSupplyAmount =
                    totalSupplyAmount.add(
                            itemParam.getSupplyAmount()
                    );

            // 전체 세액 = 기존 합계 + 현재 품목 세액
            totalTaxAmount =
                    totalTaxAmount.add(
                            itemParam.getTaxAmount()
                    );

            // 전체 금액 = 기존 합계 + 현재 품목 총금액
            totalAmount =
                    totalAmount.add(
                            itemParam.getTotalAmount()
                    );
        }

        // 7단계:
        // 모든 검증과 계산이 성공한 뒤에만 Oracle Sequence의 NEXTVAL을 사용
        // Sequence 값은 Transaction이 rollback되어도 되돌아오지 않으므로
        // 가능한 한 INSERT 직전에 호출
        Long orderNoSequence =
                purchaseOrderMapper.nextPurchaseOrderNoSequence();

        // DB에서 Sequence 값을 정상적으로 받지 못한 경우 서버 내부 오류로 처리
        if (orderNoSequence == null) {
            throw new IllegalStateException(
                    "발주번호 Sequence를 생성하지 못했습니다."
            );
        }

        // 8단계:
        // 발주일 + Sequence를 이용하여 사람이 보는 업무용 발주번호 생성
        // 예: PO-20260903-000001
        String orderNo =
                createOrderNo(
                        request,
                        orderNoSequence
                );

        // 9단계:
        // PURCHASE_ORDER Header INSERT에 전달할 객체 생성
        PurchaseOrderInsertParam headerParam =
                new PurchaseOrderInsertParam();

        // JWT에서 전달받은 현재 회사 ID
        headerParam.setCompanyId(companyId);

        // Backend에서 만든 업무용 발주번호
        headerParam.setOrderNo(orderNo);

        // React 요청에서 받은 공급업체 ID
        headerParam.setSupplierId(
                request.getSupplierId()
        );

        // React 요청에서 받은 예정 입고창고 ID
        headerParam.setWarehouseId(
                request.getWarehouseId()
        );

        // React 요청에서 받은 발주일
        headerParam.setOrderDate(
                request.getOrderDate()
        );

        // React 요청에서 받은 납품희망일
        // 입력하지 않았다면 null이 저장될 수 있음
        headerParam.setExpectedDeliveryDate(
                request.getExpectedDeliveryDate()
        );

        // 공급업체에 전달할 요청사항
        headerParam.setRequestNote(
                request.getRequestNote()
        );

        // ERP 내부 직원만 보는 메모
        headerParam.setInternalMemo(
                request.getInternalMemo()
        );

        // Frontend가 계산한 금액을 사용하지 않고 Backend 계산 결과를 저장
        headerParam.setTotalSupplyAmount(
                totalSupplyAmount
        );

        // Backend에서 계산한 전체 세액
        headerParam.setTotalTaxAmount(
                totalTaxAmount
        );

        // Backend에서 계산한 전체 최종금액
        headerParam.setTotalAmount(
                totalAmount
        );

        // 발주서를 실제 등록한 로그인 사용자 ID
        headerParam.setCreatedBy(appUserId);

        // PURCHASE_ORDER Header 한 건 INSERT
        int insertedHeaderCount =
                purchaseOrderMapper.insertPurchaseOrder(
                        headerParam
                );

        // INSERT는 정확히 한 행만 성공해야 정상
        if (insertedHeaderCount != 1) {
            throw new IllegalStateException(
                    "발주 Header 저장에 실패했습니다."
            );
        }

        // Oracle IDENTITY로 생성된 PURCHASE_ORDER_ID가
        // MyBatis useGeneratedKeys를 통해 headerParam에 채워졌는지 확인
        if (headerParam.getPurchaseOrderId() == null) {
            throw new IllegalStateException(
                    "생성된 발주 ID를 확인할 수 없습니다."
            );
        }

        // 10단계:
        // Header에서 생성된 PK를 각 발주 품목에 넣고 Item을 저장
        saveItems(
                headerParam.getPurchaseOrderId(),
                itemParams
        );

        // 11단계:
        // 발주 등록 성공 후 React로 돌려줄 응답 DTO 생성
        return new PurchaseOrderCreateResponse(
                headerParam.getPurchaseOrderId(),
                orderNo,
                APPROVAL_STATUS_DRAFT,
                RECEIPT_STATUS_NOT_RECEIVED
        );
    }

    // 발주 목록 조회 전용 Service
    // readOnly = true는 이 Transaction에서 데이터를 변경하지 않는 조회 전용이라는 뜻
    @Transactional(readOnly = true)
    public List<PurchaseOrderListResponse> getPurchaseOrders(
            Long companyId,
            Long appUserId,
            String orderNo,
            String supplierName,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            String approvalStatus,
            String receiptStatus
    ) {
        // JWT에서 넘어온 회사 ID와 사용자 ID가 정상인지 기존 Validator로 확인
        purchaseOrderValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        // 사용자가 검색창에 공백만 입력했다면 검색조건이 없는 것으로 처리
        String normalizedOrderNo =
                normalizeSearchText(orderNo);

        // 공급업체명의 앞뒤 공백을 제거하고 공백만 입력했다면 검색조건에서 제외
        String normalizedSupplierName =
                normalizeSearchText(supplierName);

        // 상태코드는 사용자가 소문자로 보내더라도 DB 코드인 대문자 형태로 통일
        String normalizedApprovalStatus =
                normalizeStatusCode(approvalStatus);

        // 입고상태도 같은 방식으로 대문자로 통일
        String normalizedReceiptStatus =
                normalizeStatusCode(receiptStatus);

        // 날짜 범위와 상태값 등이 정상인지 검사
        purchaseOrderValidator.validateListSearchConditions(
                orderDateFrom,
                orderDateTo,
                normalizedApprovalStatus,
                normalizedReceiptStatus
        );

        // 현재 회사 + 검색조건에 맞는 발주를 전부 조회
        return purchaseOrderMapper.findPurchaseOrders(
                companyId,
                normalizedOrderNo,
                normalizedSupplierName,
                orderDateFrom,
                orderDateTo,
                normalizedApprovalStatus,
                normalizedReceiptStatus
        );
    }

    // 발주 한 건의 Header와 Item을 상세조회하는 Service
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrderDetail(
            Long companyId,
            Long appUserId,
            Long purchaseOrderId
    ) {
        // 1단계:
        // Controller가 JWT에서 꺼내 전달한 회사 ID와 사용자 ID가 정상인지 확인
        purchaseOrderValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        // 2단계:
        // URL에서 받은 발주 ID가 null, 0, 음수가 아닌지 확인
        purchaseOrderValidator.validatePurchaseOrderId(
                purchaseOrderId
        );

        // 3단계:
        // 현재 로그인 회사의 발주 Header 한 건 조회
        // purchaseOrderId가 존재해도 다른 회사 발주이면 조회되지 않음
        PurchaseOrderDetailResponse response =
                purchaseOrderMapper.findPurchaseOrderDetail(
                                companyId,
                                purchaseOrderId
                        )
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.RESOURCE_NOT_FOUND,
                                        "발주 정보를 찾을 수 없습니다."
                                )
                        );

        // 4단계:
        // 같은 발주에 포함된 상품 여러 건을 조회
        List<PurchaseOrderItemDetailResponse> items =
                purchaseOrderMapper.findPurchaseOrderItems(
                        companyId,
                        purchaseOrderId
                );

        // 5단계:
        // Header DTO 안의 items 필드에 상품 목록을 넣음
        response.setItems(items);

        // 6단계:
        // Header + Item이 합쳐진 최종 상세조회 DTO를 Controller로 반환
        return response;
    }

    // 발주 한 건 수정
    @Transactional
    public PurchaseOrderDetailResponse updatePurchaseOrder(
            Long companyId,
            Long appUserId,
            Long purchaseOrderId,
            PurchaseOrderUpdateRequest request
    ) {

        // 로그인 회사와 사용자 검증
        purchaseOrderValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        // 수정 대상 발주 ID 검증
        purchaseOrderValidator.validatePurchaseOrderId(
                purchaseOrderId
        );

        // 기존 발주 Header 조회
        PurchaseOrderDetailResponse existingPurchaseOrder =
                purchaseOrderMapper.findPurchaseOrderDetail(
                                companyId,
                                purchaseOrderId
                        )
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.RESOURCE_NOT_FOUND,
                                        "발주 정보를 찾을 수 없습니다."
                                )
                        );

        // 기존 발주품목 조회
        List<PurchaseOrderItemDetailResponse> existingItems =
                purchaseOrderMapper.findPurchaseOrderItems(
                        companyId,
                        purchaseOrderId
                );

        // DRAFT + NOT_RECEIVED + 미입고 상태인지 검사
        purchaseOrderValidator.validateUpdatablePurchaseOrder(
                existingPurchaseOrder,
                existingItems
        );

        // 수정된 공급업체 검증
        purchaseOrderValidator.validateSupplier(
                companyId,
                request.getSupplierId()
        );

        // 수정된 창고 검증
        purchaseOrderValidator.validateWarehouse(
                companyId,
                request.getWarehouseId()
        );

        // 수정된 날짜 관계 검증
        purchaseOrderValidator.validateDates(request);

        // 수정 후 최종 품목 전체 검증 및 재계산
        List<PurchaseOrderItemInsertParam> itemParams =
                validateAndCalculateItems(
                        companyId,
                        request.getItems()
                );

        // Header 합계 재계산
        BigDecimal totalSupplyAmount =
                BigDecimal.ZERO.setScale(2);

        BigDecimal totalTaxAmount =
                BigDecimal.ZERO.setScale(2);

        BigDecimal totalAmount =
                BigDecimal.ZERO.setScale(2);

        for (PurchaseOrderItemInsertParam itemParam : itemParams) {

            totalSupplyAmount =
                    totalSupplyAmount.add(
                            itemParam.getSupplyAmount()
                    );

            totalTaxAmount =
                    totalTaxAmount.add(
                            itemParam.getTaxAmount()
                    );

            totalAmount =
                    totalAmount.add(
                            itemParam.getTotalAmount()
                    );
        }

        // Header UPDATE용 객체 생성
        PurchaseOrderUpdateParam updateParam =
                new PurchaseOrderUpdateParam();

        updateParam.setPurchaseOrderId(
                purchaseOrderId
        );

        updateParam.setCompanyId(
                companyId
        );

        updateParam.setSupplierId(
                request.getSupplierId()
        );

        updateParam.setWarehouseId(
                request.getWarehouseId()
        );

        updateParam.setOrderDate(
                request.getOrderDate()
        );

        updateParam.setExpectedDeliveryDate(
                request.getExpectedDeliveryDate()
        );

        updateParam.setRequestNote(
                request.getRequestNote()
        );

        updateParam.setInternalMemo(
                request.getInternalMemo()
        );

        updateParam.setTotalSupplyAmount(
                totalSupplyAmount
        );

        updateParam.setTotalTaxAmount(
                totalTaxAmount
        );

        updateParam.setTotalAmount(
                totalAmount
        );

        updateParam.setUpdatedBy(
                appUserId
        );

        // 발주 Header 수정
        int updatedHeaderCount =
                purchaseOrderMapper.updatePurchaseOrder(
                        updateParam
                );

        if (updatedHeaderCount != 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "발주 상태가 변경되었거나 수정할 수 없는 발주입니다."
            );
        }

        // 기존 발주품목 전체 삭제
        int deletedItemCount =
                purchaseOrderMapper.deletePurchaseOrderItems(
                        companyId,
                        purchaseOrderId
                );

        if (deletedItemCount != existingItems.size()) {
            throw new IllegalStateException(
                    "기존 발주 품목 삭제 결과가 일치하지 않습니다."
            );
        }

        // 수정 후 최종 발주품목 전체 저장
        saveItems(
                purchaseOrderId,
                itemParams
        );

        // 수정 완료된 Header 재조회
        PurchaseOrderDetailResponse response =
                purchaseOrderMapper.findPurchaseOrderDetail(
                                companyId,
                                purchaseOrderId
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "수정된 발주 정보를 확인할 수 없습니다."
                                )
                        );

        // 수정 완료된 품목 재조회
        List<PurchaseOrderItemDetailResponse> updatedItems =
                purchaseOrderMapper.findPurchaseOrderItems(
                        companyId,
                        purchaseOrderId
                );

        response.setItems(
                updatedItems
        );

        return response;
    }

    // 발주 승인 요청
    @Transactional
    public void requestPurchaseOrderApproval(
            Long companyId,
            Long appUserId,
            Long purchaseOrderId
    ) {
        purchaseOrderValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        purchaseOrderValidator.validatePurchaseOrderId(
                purchaseOrderId
        );

        PurchaseOrder purchaseOrder =
                purchaseOrderRepository
                        .findByPurchaseOrderIdAndCompanyId(
                                purchaseOrderId,
                                companyId
                        )
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.RESOURCE_NOT_FOUND,
                                        "발주 정보를 찾을 수 없습니다."
                                )
                        );

        if (purchaseOrder.getApprovalStatus()
                != PurchaseOrderApprovalStatus.DRAFT) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "DRAFT 상태의 발주만 승인 요청할 수 있습니다."
            );
        }

        purchaseOrder.requestApproval(
                appUserId
        );
    }

    // 실제 발주 승인
    // 하나의 발주 상태를 조회하고 변경하는 작업이므로 JPA를 사용
    @Transactional
    public void approvePurchaseOrder(
            Long companyId,
            Long appUserId,
            Long purchaseOrderId
    ) {
        purchaseOrderValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        purchaseOrderValidator.validatePurchaseOrderId(
                purchaseOrderId
        );

        PurchaseOrder purchaseOrder =
                purchaseOrderRepository
                        .findByPurchaseOrderIdAndCompanyId(
                                purchaseOrderId,
                                companyId
                        )
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.RESOURCE_NOT_FOUND,
                                        "발주 정보를 찾을 수 없습니다."
                                )
                        );

        if (purchaseOrder.getApprovalStatus()
                != PurchaseOrderApprovalStatus.PENDING) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "PENDING 상태의 발주만 승인할 수 있습니다."
            );
        }

        purchaseOrder.approve(
                appUserId
        );
    }

    // 발주 품목 전체의 기준정보를 검증하고 계산 결과를 만드는 메서드
    private List<PurchaseOrderItemInsertParam> validateAndCalculateItems(
            Long companyId,
            List<PurchaseOrderItemCreateRequest> items
    ) {

        // 검증과 계산이 끝난 품목들을 순서대로 저장
        List<PurchaseOrderItemInsertParam> itemParams =
                new ArrayList<>();

        // 같은 상품 + 같은 상품단위가 발주서에 중복 등록되는 것을 막기 위한 Set
        // Set은 동일한 값을 두 번 저장할 수 없는 자료구조
        Set<String> itemKeys =
                new HashSet<>();

        // React가 보낸 발주 품목을 한 줄씩 처리
        for (PurchaseOrderItemCreateRequest item : items) {

            // productId와 productUnitId를 묶어 한 품목의 중복검사용 Key 생성
            // 예: 상품 10 + 단위 3 -> "10:3"
            String itemKey =
                    item.getProductId()
                            + ":"
                            + item.getProductUnitId();

            // add()는 처음 들어온 값이면 true,
            // 이미 같은 값이 Set에 있으면 false를 반환
            if (!itemKeys.add(itemKey)) {

                // 같은 상품 + 같은 단위를 두 줄로 입력했다면 등록 차단
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "동일한 상품과 상품단위를 발주서에 중복 등록할 수 없습니다."
                );
            }

            // 현재 회사에서 실제 사용할 수 있는 상품과 상품단위인지 조회
            // 동시에 PRODUCT_UNIT.conversion_qty와 PRODUCT.tax_type도 가져옴
            PurchaseOrderItemReference reference =
                    purchaseOrderMapper.findItemReference(
                                    companyId,
                                    item.getProductId(),
                                    item.getProductUnitId()
                            )
                            .orElseThrow(
                                    () -> new BusinessException(
                                            ErrorCode.INVALID_REQUEST,
                                            "현재 회사에서 사용할 수 있는 상품 또는 상품단위가 아닙니다."
                                    )
                            );

            // DB에서 읽어온 환산수량과 과세유형이 정상인지 검사
            purchaseOrderValidator.validateReferenceData(
                    reference
            );

            // 발주수량, 환산수량, 매입단가, 과세유형을 기준으로
            // 기준수량과 공급가액, 세액, 총금액을 Backend에서 계산
            PurchaseOrderItemInsertParam itemParam =
                    purchaseOrderCalculator.calculateItem(
                            item,
                            reference
                    );

            // 계산까지 정상적으로 끝난 품목만 INSERT 대상 List에 추가
            itemParams.add(itemParam);
        }

        // 모든 품목의 검증과 계산이 끝난 결과를 반환
        return itemParams;
    }

    // 사람이 화면에서 볼 업무용 발주번호를 만드는 메서드
    private String createOrderNo(
            PurchaseOrderCreateRequest request,
            Long orderNoSequence
    ) {

        // 발주일을 yyyyMMdd 형태로 변환
        // 예: 2026-09-03 -> 20260903
        String orderDate =
                request.getOrderDate()
                        .format(ORDER_NO_DATE_FORMAT);

        // Sequence 숫자를 최소 6자리로 맞춤
        // 예: 1 -> 000001
        String sequencePart =
                String.format(
                        "%06d",
                        orderNoSequence
                );

        // 최종 발주번호 생성
        // 예: PO-20260903-000001
        return "PO-"
                + orderDate
                + "-"
                + sequencePart;
    }

    // 계산 완료된 발주 품목들을 PURCHASE_ORDER_ITEM에 저장
    private void saveItems(
            Long purchaseOrderId,
            List<PurchaseOrderItemInsertParam> itemParams
    ) {

        // 각 발주 품목을 한 건씩 INSERT
        for (PurchaseOrderItemInsertParam itemParam : itemParams) {

            // 먼저 생성된 PURCHASE_ORDER Header의 PK를 Item FK에 연결
            itemParam.setPurchaseOrderId(
                    purchaseOrderId
            );

            // PURCHASE_ORDER_ITEM 한 건 INSERT
            int insertedItemCount =
                    purchaseOrderMapper.insertPurchaseOrderItem(
                            itemParam
                    );

            // 품목 하나라도 정확히 한 행이 저장되지 않으면 예외 발생
            // RuntimeException 계열인 IllegalStateException이 발생하므로
            // @Transactional에 의해 앞에서 저장한 Header와 Item도 rollback 대상
            if (insertedItemCount != 1) {
                throw new IllegalStateException(
                        "발주 품목 저장에 실패했습니다."
                );
            }
        }
    }

    // 일반 검색문자열의 앞뒤 공백을 제거
    private String normalizeSearchText(
            String value
    ) {
        // 값 자체가 없거나 공백뿐이면 MyBatis 검색조건에서 제외하도록 null 반환
        if (value == null || value.isBlank()) {
            return null;
        }

        // 실제 문자가 있다면 앞뒤 공백만 제거
        return value.trim();
    }

    // 상태코드를 DB 상태값 형식인 대문자로 통일
    private String normalizeStatusCode(
            String value
    ) {
        // 상태를 선택하지 않았다면 검색조건 없음
        if (value == null || value.isBlank()) {
            return null;
        }

        // 예: "draft" -> "DRAFT"
        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}