package com.foodlogistics.erp.outbound.controller;


import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.outbound.dto.OutboundCreateRequestDto;
import com.foodlogistics.erp.outbound.dto.OutboundCancelRequestDto;
import com.foodlogistics.erp.outbound.dto.OutboundDetailResponseDto;
import com.foodlogistics.erp.outbound.dto.OutboundLotOptionDto;
import com.foodlogistics.erp.outbound.dto.OutboundResponseDto;
import com.foodlogistics.erp.outbound.dto.OutboundWarehouseOptionDto;
import com.foodlogistics.erp.outbound.service.OutboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class OutboundController {

    private final OutboundService outboundService;

    @GetMapping
    public ApiResponse<List<OutboundResponseDto>> getOutboundList(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                outboundService.getOutboundList(companyId.longValue())
        );
    }

    @GetMapping("/options/warehouses")
    public ApiResponse<List<OutboundWarehouseOptionDto>> getWarehouseOptions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                outboundService.getWarehouseOptions(companyId.longValue())
        );
    }

    @GetMapping("/options/lots")
    public ApiResponse<List<OutboundLotOptionDto>> getLotOptions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long warehouseId,
            @RequestParam Long productId
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                outboundService.getLotOptions(
                        companyId.longValue(),
                        warehouseId,
                        productId
                )
        );
    }

    @GetMapping("/{outboundId}")
    public ApiResponse<OutboundDetailResponseDto> getOutboundDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long outboundId
    ) {
        Number companyId = jwt.getClaim("companyId");

        return ApiResponse.ok(
                outboundService.getOutboundDetail(companyId.longValue(), outboundId)
        );
    }
    @PostMapping
    public ApiResponse<OutboundResponseDto> createOutbound(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody
            @Valid
            OutboundCreateRequestDto request
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                outboundService.createOutbound(
                        companyId.longValue(),
                        appUserId.longValue(),
                        request
                )
        );
    }
    @PostMapping("/{outboundId}/confirm")
    public ApiResponse<OutboundResponseDto> confirmOutbound(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long outboundId
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                outboundService.confirmOutbound(
                        companyId.longValue(),
                        appUserId.longValue(),
                        outboundId
                )
        );
    }

    @PostMapping("/{outboundId}/cancel")
    public ApiResponse<OutboundResponseDto> cancelOutbound(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long outboundId,
            @RequestBody @Valid OutboundCancelRequestDto request
    ) {
        Number companyId = jwt.getClaim("companyId");
        Number appUserId = jwt.getClaim("appUserId");

        return ApiResponse.ok(
                outboundService.cancelOutbound(
                        companyId.longValue(),
                        appUserId.longValue(),
                        outboundId,
                        request
                )
        );
    }
}
