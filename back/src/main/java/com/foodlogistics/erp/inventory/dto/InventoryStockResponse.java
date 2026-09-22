package com.foodlogistics.erp.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// STOCK 한 행: 회사·창고·상품별 현재 총재고.
// Setter는 MyBatis가 조회 결과를 채울 때, Getter는 JSON 응답을 만들 때 사용한다.
@Getter
@Setter
public class InventoryStockResponse {

    // STOCK의 행 식별자.
    private Long stockId;

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

    // 기준 단위로 저장된 현재고. 소수 정밀도를 위해 BigDecimal을 사용한다.
    // NUMBER(19,3)의 소수 정밀도를 브라우저에서도 유지하도록 숫자 문자열로 응답한다.
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal quantity;
}
