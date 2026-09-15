package com.foodlogistics.erp.warehouse;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.warehouse.dto.WarehouseResponse;
import com.foodlogistics.erp.warehouse.dto.WarehouseSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;

    public List<WarehouseResponse> getWarehouses(
            Long companyId,
            String keyword,
            String useYn
    ) {
        return warehouseMapper.findAll(
                        companyId,
                        trimToNull(keyword),
                        normalizeUseYn(useYn)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WarehouseResponse getWarehouse(
            Long companyId,
            Long warehouseId
    ) {
        return toResponse(
                findWarehouse(companyId, warehouseId)
        );
    }

    @Transactional
    public WarehouseResponse createWarehouse(
            Long companyId,
            Long appUserId,
            WarehouseSaveRequest request
    ) {
        String warehouseCode =
                normalizeWarehouseCode(
                        request.getWarehouseCode()
                );

        validateDuplicateCode(
                companyId,
                warehouseCode,
                null
        );

        Warehouse warehouse = new Warehouse();

        warehouse.setCompanyId(companyId);
        warehouse.setWarehouseCode(warehouseCode);
        warehouse.setUseYn("Y");
        warehouse.setCreatedBy(appUserId);
        warehouse.setUpdatedBy(appUserId);

        applyRequest(warehouse, request);

        warehouseMapper.insert(warehouse);

        return getWarehouse(
                companyId,
                warehouse.getWarehouseId()
        );
    }

    @Transactional
    public WarehouseResponse updateWarehouse(
            Long companyId,
            Long appUserId,
            Long warehouseId,
            WarehouseSaveRequest request
    ) {
        findWarehouse(companyId, warehouseId);

        String warehouseCode =
                normalizeWarehouseCode(
                        request.getWarehouseCode()
                );

        validateDuplicateCode(
                companyId,
                warehouseCode,
                warehouseId
        );

        Warehouse warehouse = new Warehouse();

        warehouse.setWarehouseId(warehouseId);
        warehouse.setCompanyId(companyId);
        warehouse.setWarehouseCode(warehouseCode);
        warehouse.setUpdatedBy(appUserId);

        applyRequest(warehouse, request);

        int updatedCount =
                warehouseMapper.update(warehouse);

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return getWarehouse(companyId, warehouseId);
    }

    @Transactional
    public void deactivateWarehouse(
            Long companyId,
            Long appUserId,
            Long warehouseId
    ) {
        findWarehouse(companyId, warehouseId);

        int updatedCount =
                warehouseMapper.updateUseYn(
                        companyId,
                        warehouseId,
                        "N",
                        appUserId
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private Warehouse findWarehouse(
            Long companyId,
            Long warehouseId
    ) {
        Warehouse warehouse =
                warehouseMapper.findById(
                        companyId,
                        warehouseId
                );

        if (warehouse == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return warehouse;
    }

    private void validateDuplicateCode(
            Long companyId,
            String warehouseCode,
            Long excludeWarehouseId
    ) {
        int duplicateCount =
                warehouseMapper.countByWarehouseCode(
                        companyId,
                        warehouseCode,
                        excludeWarehouseId
                );

        if (duplicateCount > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_WAREHOUSE_CODE
            );
        }
    }

    private void applyRequest(
            Warehouse warehouse,
            WarehouseSaveRequest request
    ) {
        warehouse.setWarehouseName(
                request.getWarehouseName().trim()
        );

        warehouse.setPostalCode(
                trimToNull(request.getPostalCode())
        );

        warehouse.setAddress1(
                trimToNull(request.getAddress1())
        );

        warehouse.setAddress2(
                trimToNull(request.getAddress2())
        );

        warehouse.setDescription(
                trimToNull(request.getDescription())
        );
    }

    private String normalizeWarehouseCode(
            String warehouseCode
    ) {
        return warehouseCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeUseYn(String useYn) {
        String normalized = trimToNull(useYn);

        if (normalized == null
                || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }

        normalized = normalized.toUpperCase(Locale.ROOT);

        if (!"Y".equals(normalized)
                && !"N".equals(normalized)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private WarehouseResponse toResponse(
            Warehouse warehouse
    ) {
        return new WarehouseResponse(
                warehouse.getWarehouseId(),
                warehouse.getCompanyId(),
                warehouse.getWarehouseCode(),
                warehouse.getWarehouseName(),
                warehouse.getPostalCode(),
                warehouse.getAddress1(),
                warehouse.getAddress2(),
                warehouse.getDescription(),
                warehouse.getUseYn(),
                warehouse.getCreatedAt(),
                warehouse.getCreatedBy(),
                warehouse.getUpdatedAt(),
                warehouse.getUpdatedBy()
        );
    }
}