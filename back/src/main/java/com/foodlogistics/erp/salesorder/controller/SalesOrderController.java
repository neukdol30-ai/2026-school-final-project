package com.foodlogistics.erp.salesorder.controller;


import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.salesorder.dto.SalesOrderCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderResponseDto;
import com.foodlogistics.erp.salesorder.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<List<SalesOrderResponseDto>> getSalesOrders() {
        return  ApiResponse.ok(
                salesOrderService.getSalesOrders()
        );
    }
    // 판매주문 등록
    @PostMapping
    public ApiResponse<SalesOrderResponseDto> createSalesOrder(
            @RequestBody
            @Valid
            SalesOrderCreateRequestDto request
    ) {
        return ApiResponse.ok(
                salesOrderService.createSalesOrder(request)
        );
    }
    //판매주문 확정
    @PostMapping("/{salesOrderId}/confirm")
    public ApiResponse<SalesOrderResponseDto> confirmSalesOrder(
            @PathVariable("salesOrderId") Long salesOrderId
    ) {
        return ApiResponse.ok(
                salesOrderService.confirmSalesOrder(salesOrderId)
        );
    }
}
