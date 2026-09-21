package com.foodlogistics.erp.purchase.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
// 발주 상세조회에서 상품 한 줄의 정보를 React로 전달하는 응답 DTO
public class PurchaseOrderItemDetailResponse {

    // PUCHASE_ORDER_ITEM 테이블의 실제 PK
    private Long purchaseOrderItemId;

    // 발주한 상품의 실제 PK
    private Long productId;

    // 사용자가 확인할 수 있는 상품코드
    private String productCode;

    // 사용자가 확인할 수 있는 상품명
    private String productName;

    // 발주할 때 선택했던 상품단위의 PK
    private Long productUnitId;

    // UNIT 테이블에서 조회한 단위코드
    // 예: EA, BOX
    private String unitCode;

    // 단위코드
    // 예: 개, 박스
    private String unitName;

    // 사용자가 발주할 때 입력한 수량
    // 예: 2 BOX이면 2
    private BigDecimal orderedQty;

    // 선택한 단위 하나가 기준단위 몇 개인지 나타내는 환산수량
    // 예: 1 BOX = 48 EA이면 48
    private BigDecimal conversionQty;

    // 발주수량을 기준단위로 환산한 수량
    // 예: 2 BOX x 48 EA = 96 EA
    private BigDecimal baseOrderedQty;

    // 현재까지 실제로 입고된 기준단위 수량
    // PURCHASE_ORDER_ITEM의 제약조건상 baseOrderedQty 이하의 값
    private BigDecimal receivedQty;

    // 발주 당시 입력한 매입단가
    private BigDecimal unitPrice;

    // 과세유형
    // TAXABLE 또는 TAX_FREE
    private String taxType;

    // 해당 품목의 공급가액
    private BigDecimal supplyAmount;

    // 해당 품목의 세액
    private BigDecimal taxAmount;

    // 해당 품목의 최종금액
    private BigDecimal totalAmount;
}
