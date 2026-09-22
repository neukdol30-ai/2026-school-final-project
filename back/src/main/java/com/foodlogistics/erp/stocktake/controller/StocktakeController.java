package com.foodlogistics.erp.stocktake.controller;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.stocktake.dto.StocktakeCreateRequestDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeDetailResponseDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeLotOptionDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeProductOptionDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeResponseDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeWarehouseOptionDto;
import com.foodlogistics.erp.stocktake.service.StocktakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/stocktakes")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeService stocktakeService;

    @GetMapping
    public ApiResponse<List<StocktakeResponseDto>> getStocktakeList(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.getStocktakeList(
                        companyId.longValue()
                )
        );
    }

    @GetMapping("/options/warehouses")
    public ApiResponse<List<StocktakeWarehouseOptionDto>> getWarehouseOptions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.getWarehouseOptions(companyId.longValue())
        );
    }

    @GetMapping("/options/products")
    public ApiResponse<List<StocktakeProductOptionDto>> getProductOptions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.getProductOptions(companyId.longValue())
        );
    }

    @GetMapping("/options/lots")
    public ApiResponse<List<StocktakeLotOptionDto>> getLotOptions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long warehouseId,
            @RequestParam Long productId
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.getLotOptions(
                        companyId.longValue(),
                        warehouseId,
                        productId
                )
        );
    }

    // LOT 비관리 상품의 창고별 현재 전산 재고를 조회한다.
    @GetMapping("/options/stock-quantity")
    public ApiResponse<BigDecimal> getStockQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long warehouseId,
            @RequestParam Long productId
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.getStockQuantity(
                        companyId.longValue(),
                        warehouseId,
                        productId
                )
        );
    }

    @GetMapping("/{stocktakeId}")
    public ApiResponse<StocktakeDetailResponseDto> getStocktakeDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long stocktakeId
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.getStocktakeDetail(
                        companyId.longValue(),
                        stocktakeId
                )
        );
    }

    @PostMapping
    public ApiResponse<Long> createStocktake(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid StocktakeCreateRequestDto request
    ) {
        // 로그인 토큰 안에 들어 있는 사용자·회사 정보
        Number appUserId = jwt.getClaim("appUserId");
        Number companyId = jwt.getClaim("companyId");

        Long stocktakeId = stocktakeService.createStocktake(
                companyId.longValue(),
                appUserId.longValue(),
                request
        );

        return ApiResponse.ok(stocktakeId);
    }

    // 작성중(DRAFT) 재고실사만 수정한다.
    @PutMapping("/{stocktakeId}")
    public ApiResponse<Long> updateStocktake(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long stocktakeId,
            @RequestBody @Valid StocktakeCreateRequestDto request
    ) {
        Number appUserId = jwt.getClaim("appUserId");
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                stocktakeService.updateStocktake(
                        companyId.longValue(),
                        appUserId.longValue(),
                        stocktakeId,
                        request
                )
        );
    }

    @PostMapping("/{stocktakeId}/confirm")
    public ApiResponse<StocktakeDetailResponseDto> confirmStocktake(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long stocktakeId
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                stocktakeService.confirmStocktake(
                        companyId.longValue(),
                        appUserId.longValue(),
                        stocktakeId
                )
        );
    }

    // 재고에 반영되기 전인 작성중 실사 문서만 삭제한다.
    @DeleteMapping("/{stocktakeId}")
    public ApiResponse<Long> deleteStocktake(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long stocktakeId
    ) {
        Number companyId = jwt.getClaim("companyId");

        stocktakeService.deleteStocktake(companyId.longValue(), stocktakeId);

        return ApiResponse.ok(stocktakeId);
    }
}
