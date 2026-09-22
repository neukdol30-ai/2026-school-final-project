package com.foodlogistics.erp.inventory.controller;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.inventory.dto.InventoryStockResponse;
import com.foodlogistics.erp.inventory.dto.InventoryLotResponse;
import com.foodlogistics.erp.inventory.dto.InventoryHistoryResponse;
import com.foodlogistics.erp.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 기존 로그인 인증을 그대로 사용한다. 재고를 바꾸는 POST/PUT/DELETE는 제공하지 않는다.
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    // Controller는 요청을 받고, Service에 검증·조회 처리를 맡긴다.
    private final InventoryService inventoryService;

    // 창고·상품별 STOCK 현재고 조회. 검색어는 코드 또는 이름의 일부를 받는다.
    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<List<InventoryStockResponse>>> getStocks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String warehouseKeyword,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(defaultValue = "false") String includeZero
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryService.getStocks(
                        getCompanyId(jwt), warehouseKeyword, productKeyword, includeZero
                )
        ));
    }

    // LOT 생산정보와 LOT_STOCK의 창고별 수량을 함께 조회한다.
    @GetMapping("/lots")
    public ResponseEntity<ApiResponse<List<InventoryLotResponse>>> getLots(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String warehouseKeyword,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) String lotNo,
            @RequestParam(defaultValue = "false") String includeZero
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryService.getLots(
                        getCompanyId(jwt), warehouseKeyword, productKeyword, lotNo, includeZero
                )
        ));
    }

    // 날짜를 문자열로 받아 Service에서 검사하므로 잘못된 날짜도 기존 INVALID_REQUEST로 응답한다.
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<InventoryHistoryResponse>>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String warehouseKeyword,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) String lotNo,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryService.getHistory(
                        getCompanyId(jwt), warehouseKeyword, productKeyword,
                        lotNo, movementType, startDate, endDate
                )
        ));
    }

    // 회사 ID는 검색 파라미터가 아닌 Spring Security가 검증한 JWT에서만 얻는다.
    // 로그인 사용자 ID도 함께 확인하여 인증 정보가 빠진 요청을 조회 계층으로 넘기지 않는다.
    private Long getCompanyId(Jwt jwt) {
        Number companyId = jwt == null ? null : jwt.getClaim("companyId");
        Number appUserId = jwt == null ? null : jwt.getClaim("appUserId");

        if (companyId == null || companyId.longValue() <= 0
                || appUserId == null || appUserId.longValue() <= 0) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        return companyId.longValue();
    }
}
