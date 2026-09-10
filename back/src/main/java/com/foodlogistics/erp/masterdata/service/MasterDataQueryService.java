package com.foodlogistics.erp.masterdata.service;

import com.foodlogistics.erp.masterdata.dto.AvailableProductUnitResponse;
import com.foodlogistics.erp.masterdata.dto.ProductOptionResponse;
import com.foodlogistics.erp.masterdata.dto.SupplierOptionResponse;
import com.foodlogistics.erp.masterdata.dto.WarehouseOptionResponse;
import com.foodlogistics.erp.masterdata.mapper.ProductQueryMapper;
import com.foodlogistics.erp.masterdata.mapper.SupplierQueryMapper;
import com.foodlogistics.erp.masterdata.mapper.WarehouseQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterDataQueryService {

    private final SupplierQueryMapper supplierQueryMapper;
    private final WarehouseQueryMapper warehouseQueryMapper;
    private final ProductQueryMapper productQueryMapper;

    public List<SupplierOptionResponse> getSuppliers(
            Long companyId,
            String keyword
    ) {
        return supplierQueryMapper.findActiveSuppliers(
                companyId,
                normalizeKeyword(keyword)
        );
    }

    public List<WarehouseOptionResponse> getWarehouses(
            Long companyId
    ) {
        return warehouseQueryMapper.findActiveWarehouses(
                companyId
        );
    }

    public List<ProductOptionResponse> getProducts(
            Long companyId,
            String keyword
    ) {
        return productQueryMapper.findActiveProducts(
                companyId,
                normalizeKeyword(keyword)
        );
    }

    public List<AvailableProductUnitResponse>
    getAvailableProductUnits(
            Long companyId,
            Long productId
    ) {
        return productQueryMapper.findAvailableProductUnits(
                companyId,
                productId
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }
}
