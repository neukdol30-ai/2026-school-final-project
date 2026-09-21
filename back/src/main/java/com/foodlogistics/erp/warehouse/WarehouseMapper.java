package com.foodlogistics.erp.warehouse;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarehouseMapper {

    List<Warehouse> findAll(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("useYn") String useYn
    );

    Warehouse findById(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );

    int countByWarehouseCode(
            @Param("companyId") Long companyId,
            @Param("warehouseCode") String warehouseCode,
            @Param("excludeWarehouseId") Long excludeWarehouseId
    );

    int insert(Warehouse warehouse);

    int update(Warehouse warehouse);

    int updateUseYn(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("useYn") String useYn,
            @Param("updatedBy") Long updatedBy
    );
}