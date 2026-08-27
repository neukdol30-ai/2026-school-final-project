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

    @GetMapping
    public ApiResponse<List<SalesOrderResponseDto>> getSalesOrders() {
        return  ApiResponse.ok(
                salesOrderService.getSalesOrders()
        );
    }

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
}
