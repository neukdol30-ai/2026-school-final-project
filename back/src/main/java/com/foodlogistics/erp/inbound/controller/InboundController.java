package com.foodlogistics.erp.inbound.controller;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.inbound.dto.InboundCreateRequest;
import com.foodlogistics.erp.inbound.dto.InboundCreateResponse;
import com.foodlogistics.erp.inbound.dto.InboundItemsUpdateRequest;
import com.foodlogistics.erp.inbound.dto.InboundItemsUpdateResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
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

    // 입고 조회, 생성, 품목 저장 실제 업무는 Service가 담당
    private final InboundService inboundService;

    // INBOUND DRAFT 생성
    // POST /api/inbounds
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

        log.info(
                "POST /api/inbounds: "
                        + "companyId={}, appUserId={}, purchaseOrderId={}",
                companyId.longValue(),
                appUserId.longValue(),
                request.getPurchaseOrderId()
        );

        InboundCreateResponse response =
                inboundService.createInbound(
                        companyId.longValue(),
                        appUserId.longValue(),
                        request
                );

        log.info(
                "POST /api/inbounds completed: "
                        + "companyId={}, inboundId={}, inboundNo={}",
                companyId.longValue(),
                response.getInboundId(),
                response.getInboundNo()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    // DRAFT 입고서의 품목 전체 교체 저장
    // PUT /api/inbounds/{inboundId}/items
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

        log.info(
                "PUT /api/inbounds/{}/items: "
                        + "companyId={}, appUserId={}, itemCount={}",
                inboundId,
                companyId.longValue(),
                appUserId.longValue(),
                request.getItems().size()
        );

        InboundItemsUpdateResponse response =
                inboundService.updateInboundItems(
                        companyId.longValue(),
                        appUserId.longValue(),
                        inboundId,
                        request
                );

        log.info(
                "PUT /api/inbounds/{}/items completed: "
                        + "companyId={}, savedItemCount={}, savedLotCount={}",
                inboundId,
                companyId.longValue(),
                response.getSavedItemCount(),
                response.getSavedLotCount()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    // 입고 대상 발주 목록 조회
    // GET /api/inbounds/purchase-orders
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

        log.info(
                "GET /api/inbounds/purchase-orders: companyId={}, appUserId={}",
                companyId.longValue(),
                appUserId.longValue()
        );

        List<InboundPurchaseOrderResponse> response =
                inboundService.getInboundTargetPurchaseOrders(
                        companyId.longValue(),
                        appUserId.longValue()
                );

        log.info(
                "GET /api/inbounds/purchase-orders completed: companyId={}, count={}",
                companyId.longValue(),
                response.size()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    // 선택한 발주의 입고 가능 품목 조회
    // GET /api/inbounds/purchase-orders/{purchaseOrderId}/items
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

        log.info(
                "GET /api/inbounds/purchase-orders/{}/items: "
                        + "companyId={}, appUserId={}",
                purchaseOrderId,
                companyId.longValue(),
                appUserId.longValue()
        );

        List<InboundPurchaseOrderItemResponse> response =
                inboundService.getInboundTargetPurchaseOrderItems(
                        companyId.longValue(),
                        appUserId.longValue(),
                        purchaseOrderId
                );

        log.info(
                "GET /api/inbounds/purchase-orders/{}/items completed: "
                        + "companyId={}, count={}",
                purchaseOrderId,
                companyId.longValue(),
                response.size()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }
}