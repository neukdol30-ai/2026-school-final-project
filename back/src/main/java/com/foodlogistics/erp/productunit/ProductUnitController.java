package com.foodlogistics.erp.productunit;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.productunit.dto.ProductUnitResponse;
import com.foodlogistics.erp.productunit.dto.ProductUnitSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/management/products/{productId}/units"
)
@RequiredArgsConstructor
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<
            ApiResponse<List<ProductUnitResponse>>
            > getProductUnits(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @RequestParam(required = false)
            String useYn
    ) {
        List<ProductUnitResponse> response =
                productUnitService.getProductUnits(
                        getCompanyId(jwt),
                        productId,
                        useYn
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/{productUnitId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<
            ApiResponse<ProductUnitResponse>
            > getProductUnit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @PathVariable Long productUnitId
    ) {
        ProductUnitResponse response =
                productUnitService.getProductUnit(
                        getCompanyId(jwt),
                        productId,
                        productUnitId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'PRODUCT_CREATE', 'PRODUCT_UPDATE')"
    )
    public ResponseEntity<
            ApiResponse<ProductUnitResponse>
            > createProductUnit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @Valid @RequestBody
            ProductUnitSaveRequest request
    ) {
        ProductUnitResponse response =
                productUnitService.createProductUnit(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        productId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PutMapping("/{productUnitId}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<
            ApiResponse<ProductUnitResponse>
            > updateProductUnit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @PathVariable Long productUnitId,
            @Valid @RequestBody
            ProductUnitSaveRequest request
    ) {
        ProductUnitResponse response =
                productUnitService.updateProductUnit(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        productId,
                        productUnitId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PatchMapping("/{productUnitId}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ApiResponse<Void>>
    deactivateProductUnit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @PathVariable Long productUnitId
    ) {
        productUnitService.deactivateProductUnit(
                getCompanyId(jwt),
                getAppUserId(jwt),
                productId,
                productUnitId
        );

        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    private Long getCompanyId(Jwt jwt) {
        Number companyId = jwt.getClaim(
                "companyId"
        );

        return companyId.longValue();
    }

    private Long getAppUserId(Jwt jwt) {
        Number appUserId = jwt.getClaim(
                "appUserId"
        );

        return appUserId.longValue();
    }
}