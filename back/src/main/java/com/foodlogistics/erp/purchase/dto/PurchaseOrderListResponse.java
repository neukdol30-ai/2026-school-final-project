package com.foodlogistics.erp.purchase.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
// 발주 목록 화면의 한 행을 React로 전달하는 응답 DTO
public class PurchaseOrderListResponse {

    // 화면에는 굳이 표시 안 해도 되지만, 나중에 행 클릭 -> 발주 상세 조회로 이동할 때 사용할 실제 PK
    private Long purchaseOrderId;

    // 사용자가 화면에서 보는 업무용 발주번호
    private String orderNo;

    // BUSINESS_PARTNER에서 조회한 공급업체명
    private String supplierName;

    // WAREHOUSE에서 조회한 예정 입고창고명
    private String warehouseName;

    // 발주일
    private LocalDate orderDate;

    // 납품희망일(입력하지 않은 발주라면 null 가능)
    private LocalDate expectedDeliveryDate;

    // DB에는 DRAFT, PENDING, APPROVED, REJECTED 코드로 저장
    private String approvalStatus;

    // DB에는 NOT_RECEIVED, PARTIAL, RECEIVED, CLOSED 코드로 저장
    private String receiptStatus;

    // 발주 Header에 저장된 최종 총금액
    private BigDecimal totalAmount;
}
