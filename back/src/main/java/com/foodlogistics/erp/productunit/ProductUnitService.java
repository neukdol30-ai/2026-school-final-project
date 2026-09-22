package com.foodlogistics.erp.productunit;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.product.ProductMapper;
import com.foodlogistics.erp.productunit.dto.ProductUnitResponse;
import com.foodlogistics.erp.productunit.dto.ProductUnitSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductUnitService {

    private final ProductUnitMapper productUnitMapper;
    private final ProductMapper productMapper;

    public List<ProductUnitResponse> getProductUnits(
            Long companyId,
            Long productId,
            String useYn
    ) {
        validateProduct(companyId, productId);

        return productUnitMapper.findAll(
                        companyId,
                        productId,
                        normalizeUseYn(useYn)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductUnitResponse getProductUnit(
            Long companyId,
            Long productId,
            Long productUnitId
    ) {
        validateProduct(companyId, productId);

        return toResponse(
                findProductUnit(
                        companyId,
                        productId,
                        productUnitId
                )
        );
    }

    @Transactional
    public ProductUnitResponse createProductUnit(
            Long companyId,
            Long appUserId,
            Long productId,
            ProductUnitSaveRequest request
    ) {
        validateProduct(companyId, productId);
        validateActiveUnit(request.getUnitId());

        validateDuplicateUnit(
                companyId,
                productId,
                request.getUnitId(),
                null
        );

        validateBaseUnit(
                companyId,
                productId,
                request.getIsBaseYn(),
                request.getConversionQty(),
                null
        );

        ProductUnit productUnit = new ProductUnit();

        productUnit.setProductId(productId);
        productUnit.setUnitId(request.getUnitId());
        productUnit.setConversionQty(
                request.getConversionQty()
        );
        productUnit.setIsBaseYn(
                request.getIsBaseYn()
        );
        productUnit.setUseYn("Y");
        productUnit.setCreatedBy(appUserId);
        productUnit.setUpdatedBy(appUserId);

        productUnitMapper.insert(productUnit);

        return getProductUnit(
                companyId,
                productId,
                productUnit.getProductUnitId()
        );
    }

    @Transactional
    public ProductUnitResponse updateProductUnit(
            Long companyId,
            Long appUserId,
            Long productId,
            Long productUnitId,
            ProductUnitSaveRequest request
    ) {
        validateProduct(companyId, productId);

        findProductUnit(
                companyId,
                productId,
                productUnitId
        );

        validateActiveUnit(request.getUnitId());

        validateDuplicateUnit(
                companyId,
                productId,
                request.getUnitId(),
                productUnitId
        );

        validateBaseUnit(
                companyId,
                productId,
                request.getIsBaseYn(),
                request.getConversionQty(),
                productUnitId
        );

        ProductUnit productUnit = new ProductUnit();

        productUnit.setProductUnitId(productUnitId);
        productUnit.setProductId(productId);
        productUnit.setUnitId(request.getUnitId());
        productUnit.setConversionQty(
                request.getConversionQty()
        );
        productUnit.setIsBaseYn(
                request.getIsBaseYn()
        );
        productUnit.setUpdatedBy(appUserId);

        int updatedCount =
                productUnitMapper.update(
                        companyId,
                        productUnit
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return getProductUnit(
                companyId,
                productId,
                productUnitId
        );
    }

    @Transactional
    public void deactivateProductUnit(
            Long companyId,
            Long appUserId,
            Long productId,
            Long productUnitId
    ) {
        validateProduct(companyId, productId);

        findProductUnit(
                companyId,
                productId,
                productUnitId
        );

        int updatedCount =
                productUnitMapper.updateUseYn(
                        companyId,
                        productId,
                        productUnitId,
                        "N",
                        appUserId
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private void validateProduct(
            Long companyId,
            Long productId
    ) {
        if (productMapper.findById(
                companyId,
                productId
        ) == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private ProductUnit findProductUnit(
            Long companyId,
            Long productId,
            Long productUnitId
    ) {
        ProductUnit productUnit =
                productUnitMapper.findById(
                        companyId,
                        productId,
                        productUnitId
                );

        if (productUnit == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return productUnit;
    }

    private void validateActiveUnit(Long unitId) {
        if (productUnitMapper
                .countActiveUnitById(unitId) == 0) {
            throw new BusinessException(
                    ErrorCode.UNIT_NOT_AVAILABLE
            );
        }
    }

    private void validateDuplicateUnit(
            Long companyId,
            Long productId,
            Long unitId,
            Long excludeProductUnitId
    ) {
        int duplicateCount =
                productUnitMapper
                        .countByProductAndUnit(
                                companyId,
                                productId,
                                unitId,
                                excludeProductUnitId
                        );

        if (duplicateCount > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_PRODUCT_UNIT
            );
        }
    }

    private void validateBaseUnit(
            Long companyId,
            Long productId,
            String isBaseYn,
            BigDecimal conversionQty,
            Long excludeProductUnitId
    ) {
        if (!"Y".equals(isBaseYn)) {
            return;
        }

        if (conversionQty.compareTo(
                BigDecimal.ONE
        ) != 0) {
            throw new BusinessException(
                    ErrorCode.BASE_UNIT_CONVERSION_INVALID
            );
        }

        int baseUnitCount =
                productUnitMapper.countBaseUnit(
                        companyId,
                        productId,
                        excludeProductUnitId
                );

        if (baseUnitCount > 0) {
            throw new BusinessException(
                    ErrorCode.BASE_UNIT_ALREADY_EXISTS
            );
        }
    }

    private String normalizeUseYn(String useYn) {
        String normalized = trimToNull(useYn);

        if (normalized == null
                || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }

        normalized =
                normalized.toUpperCase(Locale.ROOT);

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

    private ProductUnitResponse toResponse(
            ProductUnit productUnit
    ) {
        return new ProductUnitResponse(
                productUnit.getProductUnitId(),
                productUnit.getProductId(),
                productUnit.getUnitId(),
                productUnit.getUnitCode(),
                productUnit.getUnitName(),
                productUnit.getConversionQty(),
                productUnit.getIsBaseYn(),
                productUnit.getUseYn(),
                productUnit.getCreatedAt(),
                productUnit.getCreatedBy(),
                productUnit.getUpdatedAt(),
                productUnit.getUpdatedBy()
        );
    }
}