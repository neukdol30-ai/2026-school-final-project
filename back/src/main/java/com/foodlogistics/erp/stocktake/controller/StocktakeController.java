package com.foodlogistics.erp.stocktake.controller;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.stocktake.dto.StocktakeCreateRequestDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeResponseDto;
import com.foodlogistics.erp.stocktake.service.StocktakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
}