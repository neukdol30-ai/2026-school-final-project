package com.foodlogistics.erp.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// LOT 생산정보와 LOT_STOCK의 창고별 수량을 함께 전달한다.
// Setter는 MyBatis가 조회 결과를 채울 때, Getter는 JSON 응답을 만들 때 사용한다.
@Getter
@Setter
public class InventoryLotResponse {

    // LOT_STOCK의 행 식별자.
    private Long lotStockId;

    // LOT의 행 식별자.
    private Long lotId;

    // 제조사·공급업체의 생산 LOT 번호.
    private String lotNo;

    // 창고 식별자: 화면의 행 식별 등에 사용하며 표에 직접 노출하지 않는다.
    private Long warehouseId;

    // 사용자가 확인하거나 검색하는 창고 코드.
    private String warehouseCode;

    // 현재 창고 이름.
    private String warehouseName;

    // 상품 식별자.
    private Long productId;

    // 사용자가 확인하는 상품 코드.
    private String productCode;

    // 현재 상품 이름.
    private String productName;

    // PRODUCT_UNIT의 기준 단위와 UNIT을 연결한 실제 단위 코드.
    private String baseUnitCode;

    // 단위가 등록되지 않았다면 null이며 Frontend에서 임의 단위를 만들지 않는다.
    private String baseUnitName;

    // 제조일. 등록되지 않았다면 null.
    private LocalDate manufactureDate;

    // 화면에서는 소비기한으로 표시하고 기존 API 필드명은 유지한다.
    private LocalDate expiryDate;

    // 해당 창고의 LOT 현재고.
    // NUMBER(19,3)의 소수 정밀도를 브라우저에서도 유지하도록 숫자 문자열로 응답한다.
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal quantity;
}
