package com.foodlogistics.erp.salesorder.controller;


import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.salesorder.dto.SalesOrderCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderDetailResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderResponseDto;
import com.foodlogistics.erp.salesorder.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    // 판매주문 목록 조회
    @GetMapping
    public ApiResponse<List<SalesOrderResponseDto>> getSalesOrders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId = jwt.getClaim("companyId");

        return  ApiResponse.ok(
                salesOrderService.getSalesOrders(companyId.longValue())
        );
    }
    // 주문번호 선택 시 주문 헤더와 품목 목록 함께 조회
    @GetMapping("/{salesOrderId}")
    public ApiResponse<SalesOrderDetailResponseDto> getSalesOrderDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("salesOrderId") Long salesOrderId
    )  {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                salesOrderService.getSalesOrderDetail(
                        companyId.longValue(),
                        salesOrderId
                )
        );
    }
    // 판매주문 등록
    @PostMapping
    public ApiResponse<SalesOrderResponseDto> createSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody
            @Valid
            SalesOrderCreateRequestDto request
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                salesOrderService.createSalesOrder(
                        companyId.longValue(),
                        appUserId.longValue(),
                        request
                )
        );
    }

    // 작성중(DRAFT) 판매주문만 수정한다.
    @PutMapping("/{salesOrderId}")
    public ApiResponse<SalesOrderResponseDto> updateSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long salesOrderId,
            @RequestBody @Valid SalesOrderCreateRequestDto request
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                salesOrderService.updateSalesOrder(
                        companyId.longValue(),
                        appUserId.longValue(),
                        salesOrderId,
                        request
                )
        );
    }
    //판매주문 확정
    @PostMapping("/{salesOrderId}/confirm")
    public ApiResponse<SalesOrderResponseDto> confirmSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("salesOrderId") Long salesOrderId
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                salesOrderService.confirmSalesOrder(
                        companyId.longValue(),
                        appUserId.longValue(),
                        salesOrderId
                )
        );
    }

    // 미출고 상태의 판매주문만 취소한다.
    @PostMapping("/{salesOrderId}/cancel")
    public ApiResponse<SalesOrderResponseDto> cancelSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long salesOrderId
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                salesOrderService.cancelSalesOrder(
                        companyId.longValue(),
                        appUserId.longValue(),
                        salesOrderId
                )
        );
    }
}
