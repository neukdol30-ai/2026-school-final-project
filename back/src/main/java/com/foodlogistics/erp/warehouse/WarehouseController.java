package com.foodlogistics.erp.warehouse;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.warehouse.dto.WarehouseResponse;
import com.foodlogistics.erp.warehouse.dto.WarehouseSaveRequest;
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
@RequestMapping("/api/management/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')")
    public ResponseEntity<
            ApiResponse<List<WarehouseResponse>>
            > getWarehouses(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            String useYn
    ) {
        List<WarehouseResponse> response =
                warehouseService.getWarehouses(
                        getCompanyId(jwt),
                        keyword,
                        useYn
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')")
    public ResponseEntity<
            ApiResponse<WarehouseResponse>
            > getWarehouse(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long warehouseId
    ) {
        WarehouseResponse response =
                warehouseService.getWarehouse(
                        getCompanyId(jwt),
                        warehouseId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_CREATE')")
    public ResponseEntity<
            ApiResponse<WarehouseResponse>
            > createWarehouse(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            WarehouseSaveRequest request
    ) {
        WarehouseResponse response =
                warehouseService.createWarehouse(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')")
    public ResponseEntity<
            ApiResponse<WarehouseResponse>
            > updateWarehouse(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long warehouseId,
            @Valid @RequestBody
            WarehouseSaveRequest request
    ) {
        WarehouseResponse response =
                warehouseService.updateWarehouse(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        warehouseId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PatchMapping("/{warehouseId}/deactivate")
    @PreAuthorize("hasAuthority('WAREHOUSE_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>>
    deactivateWarehouse(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long warehouseId
    ) {
        warehouseService.deactivateWarehouse(
                getCompanyId(jwt),
                getAppUserId(jwt),
                warehouseId
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