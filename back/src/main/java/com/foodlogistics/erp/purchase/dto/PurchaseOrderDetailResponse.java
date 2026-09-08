package com.foodlogistics.erp.purchase.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// 발주 한 건의 Header 정보와 발주품목 목록을 React로 전달하는 응답 DTO
public class PurchaseOrderDetailResponse {

    // PURCHASE_ORDER 테이블의 실제 PK
    private Long purchaseOrderId;

    // 사용자가 화면에서 보는 업무용 발주번호
    private String orderNo;

    // 공급업체 PK
    // 나중에 수정 화면에서 기존 공급업체를 다시 선택 상태로 만들 때 사용할 수 있음
    private Long supplierId;

    // 사용자가 화면에서 확인할 공급업체명
    private String supplierName;

    // 예정 입고창고 PK
    private Long warehouseId;

    // 사용자가 화면에서 확인할 창고명
    private String warehouseName;

    // 발주일
    private LocalDate orderDate;

    // 납품희망일
    // 입력하지 않았다면 null 가능
    private LocalDate expectedDeliveryDate;

    // DRAFT / PENDING / APPROVED / REJECTED
    private String approvalStatus;

    // NOT_RECEIVED / PARTIAL / RECEIVED / CLOSED
    private String receiptStatus;

    // 공급업체에 전달하기 위한 요청사항
    private String requestNote;

    // ERP 내부 직원만 확인하는 메모
    private String internalMemo;

    // 발주 전체 공급가액
    private BigDecimal totalSupplyAmount;

    // 발주 전체 세액
    private BigDecimal totalTaxAmount;

    // 발주 전체 최종금액
    private BigDecimal totalAmount;

    // 최초 등록일시
    private LocalDateTime createdAt;

    // 최종 수정일시
    private LocalDateTime updatedAt;

    // 승인 완료일시
    // 아직 승인되지 않았다면 null 가능
    private LocalDateTime approvedAt;

    // 이 발주서에 포함된 상품 여러 건
    private List<PurchaseOrderItemDetailResponse> items;
}
