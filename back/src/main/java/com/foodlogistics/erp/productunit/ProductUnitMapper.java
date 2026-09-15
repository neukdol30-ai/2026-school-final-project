package com.foodlogistics.erp.productunit;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductUnitMapper {

    List<ProductUnit> findAll(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("useYn") String useYn
    );

    ProductUnit findById(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("productUnitId") Long productUnitId
    );

    int countByProductAndUnit(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("unitId") Long unitId,
            @Param("excludeProductUnitId")
            Long excludeProductUnitId
    );

    int countBaseUnit(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("excludeProductUnitId")
            Long excludeProductUnitId
    );

    int countActiveUnitById(
            @Param("unitId") Long unitId
    );

    int insert(ProductUnit productUnit);

    int update(
            @Param("companyId") Long companyId,
            @Param("productUnit") ProductUnit productUnit
    );

    int updateUseYn(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("productUnitId") Long productUnitId,
            @Param("useYn") String useYn,
            @Param("updatedBy") Long updatedBy
    );
}