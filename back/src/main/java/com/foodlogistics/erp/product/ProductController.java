package com.foodlogistics.erp.product;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.product.dto.ProductResponse;
import com.foodlogistics.erp.product.dto.ProductSaveRequest;
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
@RequestMapping("/api/management/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<
            ApiResponse<List<ProductResponse>>
            > getProducts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            String lotManagedYn,
            @RequestParam(required = false)
            String taxType,
            @RequestParam(required = false)
            String storageType,
            @RequestParam(required = false)
            String useYn
    ) {
        List<ProductResponse> response =
                productService.getProducts(
                        getCompanyId(jwt),
                        keyword,
                        lotManagedYn,
                        taxType,
                        storageType,
                        useYn
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<
            ApiResponse<ProductResponse>
            > getProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        ProductResponse response =
                productService.getProduct(
                        getCompanyId(jwt),
                        productId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ') and hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<
            ApiResponse<ProductResponse>
            > createProduct(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ProductSaveRequest request
    ) {
        ProductResponse response =
                productService.createProduct(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ') and hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<
            ApiResponse<ProductResponse>
            > updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @Valid @RequestBody
            ProductSaveRequest request
    ) {
        ProductResponse response =
                productService.updateProduct(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        productId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PatchMapping("/{productId}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCT_READ') and hasAuthority('PRODUCT_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>>
    deactivateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        productService.deactivateProduct(
                getCompanyId(jwt),
                getAppUserId(jwt),
                productId
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
