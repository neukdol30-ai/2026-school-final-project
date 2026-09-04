package com.foodlogistics.erp.purchase.controller;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateRequest;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateResponse;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderListResponse;
import com.foodlogistics.erp.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    // 발주 검증, 계산, 발주번호 생성, DB 저장 등 실제 업무는 Service가 담당
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderCreateResponse>>
    createPurchaseOrder(

            // 로그인 성공 후 클라이언트가 Authorization 헤더로 보낸 JWT를 Spring Security가 먼저 검증한 뒤 이 매개변수에 전달
            @AuthenticationPrincipal Jwt jwt,

            // React가 보낸 JSON을 PurchaseOrderCreateRequest로 변환하고 @Valid가 DTO의 Validation 조건을 검사함
            @Valid @RequestBody PurchaseOrderCreateRequest request
    ) {

        // JwtTokenProvider에서 JWT 생성 시 넣어 둔 appUserId Claim을 가져옴
        Number appUserId = jwt.getClaim("appUserId");

        // JWT 안에 저장된 현재 로그인 사용자의 회사 ID
        // 멀티테넌트 구조에서 다른 회사의 데이터와 섞이지 않도록 사용
        Number companyId = jwt.getClaim("companyId");

        // JWT에서 가져온 회사 ID와 사용자 ID, React에서 받은 발주 요청 DTO를 Service에 전달
        PurchaseOrderCreateResponse response =
                purchaseOrderService.createPurchaseOrder(
                        companyId.longValue(),
                        appUserId.longValue(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    // 발주 목록 조회
    // GET /api/purchase-orders
    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderListResponse>>>
    getPurchaseOrders(

            // 로그인 후 검증된 JWT를 Spring Security가 전달
            @AuthenticationPrincipal Jwt jwt,

            // 발주번호 검색
            // 값이 없으면 전체 발주번호를 대상으로 조회
            @RequestParam(required = false)
            String orderNo,

            // 공급업체 필터
            // 화면의 공급업체 선택창에서 partner_id를 전달
            @RequestParam(required = false)
            Long supplierId,

            // 발주일 조회 시작일
            // 예: 2026-09-01
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate orderDateFrom,

            // 발주일 조회 종료일
            // 예: 2026-09-30
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate orderDateTo,

            // DRAFT / PENDING / APPROVED / REJECTED
            @RequestParam(required = false)
            String approvalStatus,

            // NOT RECEIVED / PARTIAL / RECEIVED / CLOSED
            @RequestParam(required = false)
            String receiptStatus
    ) {

        // JWT 생성 시 JwtTokenProvider가 넣어 둔 로그인 사용자 ID
        Number appUserId =
                jwt.getClaim("appUserId");

        // JWT에 들어 있는 현재 로그인 회사 ID
        // Frontend가 회사 ID를 직접 보내지 않음
        Number companyId =
                jwt.getClaim("companyId");

        // Service에서 검색조건 검증 후 Oracle 목록 조회
        List<PurchaseOrderListResponse> response =
                purchaseOrderService.getPurchaseOrders(
                        companyId.longValue(),
                        appUserId.longValue(),
                        orderNo,
                        supplierId,
                        orderDateFrom,
                        orderDateTo,
                        approvalStatus,
                        receiptStatus
                );
        // 기존 프로젝트 공통 ApiResponse 형식으로 반환
        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }
}
