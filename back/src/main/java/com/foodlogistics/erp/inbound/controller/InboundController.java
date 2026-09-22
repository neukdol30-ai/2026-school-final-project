package com.foodlogistics.erp.inbound.controller;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.inbound.dto.*;
import com.foodlogistics.erp.inbound.service.InboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @PostMapping
    public ResponseEntity<
            ApiResponse<InboundCreateResponse>
            >
    createInbound(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InboundCreateRequest request
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        InboundCreateResponse response =
                inboundService.createInbound(
                        companyId.longValue(),
                        appUserId.longValue(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<
            ApiResponse<List<InboundPurchaseOrderResponse>>
            >
    getInboundTargetPurchaseOrders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        List<InboundPurchaseOrderResponse> response =
                inboundService.getInboundTargetPurchaseOrders(
                        companyId.longValue(),
                        appUserId.longValue()
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/purchase-orders/{purchaseOrderId}/items")
    public ResponseEntity<
            ApiResponse<List<InboundPurchaseOrderItemResponse>>
            >
    getInboundTargetPurchaseOrderItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long purchaseOrderId
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        List<InboundPurchaseOrderItemResponse> response =
                inboundService.getInboundTargetPurchaseOrderItems(
                        companyId.longValue(),
                        appUserId.longValue(),
                        purchaseOrderId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/{inboundId}")
    public ResponseEntity<
            ApiResponse<InboundDetailResponse>
            >
    getInboundDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long inboundId
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        InboundDetailResponse response =
                inboundService.getInboundDetail(
                        companyId.longValue(),
                        appUserId.longValue(),
                        inboundId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PutMapping("/{inboundId}/items")
    public ResponseEntity<
            ApiResponse<InboundItemsUpdateResponse>
            >
    updateInboundItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long inboundId,
            @Valid @RequestBody InboundItemsUpdateRequest request
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        InboundItemsUpdateResponse response =
                inboundService.updateInboundItems(
                        companyId.longValue(),
                        appUserId.longValue(),
                        inboundId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping("/{inboundId}/confirm")
    public ResponseEntity<
            ApiResponse<Long>
            >
    confirmInbound(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long inboundId
    ) {
        Number appUserId =
                jwt.getClaim("appUserId");

        Number companyId =
                jwt.getClaim("companyId");

        log.info(
                "POST /api/inbounds/{}/confirm: "
                        + "companyId={}, appUserId={}",
                inboundId,
                companyId.longValue(),
                appUserId.longValue()
        );

        Long response =
                inboundService.confirmInbound(
                        companyId.longValue(),
                        appUserId.longValue(),
                        inboundId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }
}