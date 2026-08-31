package com.foodlogistics.erp.purchase.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface PurchaseOrderMapper {

    int countAvailableSupplier(
            @Param("companyId") Long companyId,
            @Param("supplierId") Long supplierId
    );

    int countAvailableWarehouse(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );

    Optional<PurchaseOrderItemReference> findItemReference(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("productUnitId") Long productUnitId
    );

    int insertPurchaseOrder(PurchaseOrderInsertParam insertParam);

    int insertPurchaseOrderItem(PurchaseOrderItemInsertParam insertParam);
}
