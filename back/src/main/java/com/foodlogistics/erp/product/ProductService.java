package com.foodlogistics.erp.product;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.product.dto.ProductResponse;
import com.foodlogistics.erp.product.dto.ProductSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> YES_NO_VALUES =
            Set.of("Y", "N");

    private static final Set<String> TAX_TYPES =
            Set.of("TAXABLE", "TAX_FREE");

    private static final Set<String> STORAGE_TYPES =
            Set.of("AMBIENT", "CHILLED", "FROZEN");

    private final ProductMapper productMapper;

    public List<ProductResponse> getProducts(
            Long companyId,
            String keyword,
            String lotManagedYn,
            String taxType,
            String storageType,
            String useYn
    ) {
        return productMapper.findAll(
                        companyId,
                        trimToNull(keyword),
                        normalizeFilter(
                                lotManagedYn,
                                YES_NO_VALUES
                        ),
                        normalizeFilter(
                                taxType,
                                TAX_TYPES
                        ),
                        normalizeFilter(
                                storageType,
                                STORAGE_TYPES
                        ),
                        normalizeFilter(
                                useYn,
                                YES_NO_VALUES
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProduct(
            Long companyId,
            Long productId
    ) {
        return toResponse(
                findProduct(companyId, productId)
        );
    }

    @Transactional
    public ProductResponse createProduct(
            Long companyId,
            Long appUserId,
            ProductSaveRequest request
    ) {
        String productCode =
                normalizeProductCode(
                        request.getProductCode()
                );

        validateDuplicateCode(
                companyId,
                productCode,
                null
        );

        Product product = new Product();

        product.setCompanyId(companyId);
        product.setProductCode(productCode);
        product.setUseYn("Y");
        product.setCreatedBy(appUserId);
        product.setUpdatedBy(appUserId);

        applyRequest(product, request);

        productMapper.insert(product);

        return getProduct(
                companyId,
                product.getProductId()
        );
    }

    @Transactional
    public ProductResponse updateProduct(
            Long companyId,
            Long appUserId,
            Long productId,
            ProductSaveRequest request
    ) {
        findProduct(companyId, productId);

        String productCode =
                normalizeProductCode(
                        request.getProductCode()
                );

        validateDuplicateCode(
                companyId,
                productCode,
                productId
        );

        Product product = new Product();

        product.setProductId(productId);
        product.setCompanyId(companyId);
        product.setProductCode(productCode);
        product.setUpdatedBy(appUserId);

        applyRequest(product, request);

        int updatedCount =
                productMapper.update(product);

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return getProduct(companyId, productId);
    }

    @Transactional
    public void deactivateProduct(
            Long companyId,
            Long appUserId,
            Long productId
    ) {
        findProduct(companyId, productId);

        int updatedCount =
                productMapper.updateUseYn(
                        companyId,
                        productId,
                        "N",
                        appUserId
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private Product findProduct(
            Long companyId,
            Long productId
    ) {
        Product product =
                productMapper.findById(
                        companyId,
                        productId
                );

        if (product == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return product;
    }

    private void validateDuplicateCode(
            Long companyId,
            String productCode,
            Long excludeProductId
    ) {
        int duplicateCount =
                productMapper.countByProductCode(
                        companyId,
                        productCode,
                        excludeProductId
                );

        if (duplicateCount > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_PRODUCT_CODE
            );
        }
    }

    private void applyRequest(
            Product product,
            ProductSaveRequest request
    ) {
        product.setProductName(
                request.getProductName().trim()
        );

        product.setLotManagedYn(
                request.getLotManagedYn()
        );

        product.setTaxType(
                request.getTaxType()
        );

        product.setStorageType(
                request.getStorageType()
        );
    }

    private String normalizeProductCode(
            String productCode
    ) {
        return productCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeFilter(
            String value,
            Set<String> allowedValues
    ) {
        String normalized = trimToNull(value);

        if (normalized == null
                || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }

        normalized =
                normalized.toUpperCase(Locale.ROOT);

        if (!allowedValues.contains(normalized)) {
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

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getCompanyId(),
                product.getProductCode(),
                product.getProductName(),
                product.getLotManagedYn(),
                product.getTaxType(),
                product.getStorageType(),
                product.getUseYn(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy()
        );
    }
}