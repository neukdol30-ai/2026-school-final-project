package com.foodlogistics.erp.purchase.mapper;

import com.foodlogistics.erp.purchase.dto.PurchaseOrderListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PurchaseOrderMapper {

    // 현재 회사에서 사용 가능한 공급업체인지 확인
    int countAvailableSupplier(
            @Param("companyId") Long companyId,
            @Param("supplierId") Long supplierId
    );

    // 현재 회사에서 사용 가능한 창고인지 확인
    int countAvailableWarehouse(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );

    // 상품과 상품단위가 유효한지 확인 / 수량 환산값(conversionQty)과 과세유형(taxType)을 가져옵니다.
    Optional<PurchaseOrderItemReference> findItemReference(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("productUnitId") Long productUnitId
    );

    // ORDER_NO(발주번호)에 사용할 Oracle Sequence의 다음 숫자를 가져옴.
    // 예: 1 -> PO-20260903-000001을 만들 때 사용
    Long nextPurchaseOrderNoSequence();

    // PURCHASE_ORDER(발주 Header) 한 건을 저장
    int insertPurchaseOrder(PurchaseOrderInsertParam insertParam);

    // PURCHASE_ORDER_ITEM(발주 품목) 한 건을 저장.
    int insertPurchaseOrderItem(PurchaseOrderItemInsertParam insertParam);

    // 현재 로그인 회사의 발주 목록을 검색조건에 맞춰 조회
    // 페이지네이션 없이 조건에 해당하는 전체 결과를 반환
    List<PurchaseOrderListResponse> findPurchaseOrders(
            @Param("companyId") Long companyId,
            @Param("orderNo") String orderNo,
            @Param("supplierId") Long supplierId,
            @Param("orderDateFrom") LocalDate orderDateFrom,
            @Param("orderDateTo") LocalDate orderDateTo,
            @Param("approvalStatus") String approvalStatus,
            @Param("receiptStatus") String receiptStatus
            );
}
