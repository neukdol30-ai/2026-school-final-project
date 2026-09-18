package com.foodlogistics.erp.masterdata.controller;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.masterdata.dto.AvailableProductUnitResponse;
import com.foodlogistics.erp.masterdata.dto.ProductOptionResponse;
import com.foodlogistics.erp.masterdata.dto.SupplierOptionResponse;
import com.foodlogistics.erp.masterdata.dto.WarehouseOptionResponse;
import com.foodlogistics.erp.masterdata.service.MasterDataQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MasterDataQueryController {

    private final MasterDataQueryService masterDataQueryService;

    @GetMapping("/api/suppliers")
    public ResponseEntity<
            ApiResponse<List<SupplierOptionResponse>>
            > getSuppliers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            String keyword
    ) {
        List<SupplierOptionResponse> response =
                masterDataQueryService.getSuppliers(
                        getCompanyId(jwt),
                        keyword
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/api/warehouses")
    public ResponseEntity<
            ApiResponse<List<WarehouseOptionResponse>>
            > getWarehouses(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<WarehouseOptionResponse> response =
                masterDataQueryService.getWarehouses(
                        getCompanyId(jwt)
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/api/products")
    public ResponseEntity<
            ApiResponse<List<ProductOptionResponse>>
            > getProducts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            String keyword
    ) {
        List<ProductOptionResponse> response =
                masterDataQueryService.getProducts(
                        getCompanyId(jwt),
                        keyword
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/api/products/{productId}/units")
    public ResponseEntity<
            ApiResponse<List<AvailableProductUnitResponse>>
            > getAvailableProductUnits(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        List<AvailableProductUnitResponse> response =
                masterDataQueryService
                        .getAvailableProductUnits(
                                getCompanyId(jwt),
                                productId
                        );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    private Long getCompanyId(Jwt jwt) {
        Number companyId = jwt.getClaim("companyId");

        return companyId.longValue();
    }
}