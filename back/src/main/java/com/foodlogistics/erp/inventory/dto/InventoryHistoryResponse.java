package com.foodlogistics.erp.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 기존 이력 값과 연결된 업무 문서번호를 전달하는 조회 응답.
// Setter는 MyBatis가 조회 결과를 채울 때, Getter는 JSON 응답을 만들 때 사용한다.
@Getter
@Setter
public class InventoryHistoryResponse {

    // 재고 이력의 행 식별자.
    private Long stockHistoryId;

    // 한국시간으로 저장된 발생일시. 시간대를 다시 변환하지 않는다.
    private LocalDateTime createdAt;

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

    // LOT가 없는 일반 상품 이력은 null.
    private Long lotId;

    // 이력에 연결된 생산 LOT 번호.
    private String lotNo;

    // 기존 DB 변동유형을 그대로 보존한다. 한국어 변환은 화면에서 한다.
    private String movementType;

    // 증가이면 양수, 감소이면 음수인 기존 증감 수량.
    // NUMBER(19,3)의 소수 정밀도를 브라우저에서도 유지하도록 숫자 문자열로 응답한다.
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal changeQty;

    // 기존 업무 출처를 movementType과 합치지 않고 그대로 전달한다.
    private String sourceType;

    // 원래 업무 문서 ID. 문서번호가 없을 때만 화면의 대체 표시로 사용한다.
    private Long sourceId;

    // 출처에 따라 입고번호·출고번호·실사번호를 연결한 값.
    private String sourceNo;
}
