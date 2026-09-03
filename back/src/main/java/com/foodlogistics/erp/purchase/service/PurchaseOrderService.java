package com.foodlogistics.erp.purchase.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.purchase.calculator.PurchaseOrderCalculator;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateRequest;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateResponse;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderItemCreateRequest;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemReference;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderMapper;
import com.foodlogistics.erp.purchase.validator.PurchaseOrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}