package com.foodlogistics.erp.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> findAll(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("lotManagedYn") String lotManagedYn,
            @Param("taxType") String taxType,
            @Param("storageType") String storageType,
            @Param("useYn") String useYn
    );

    Product findById(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId
    );

    int countByProductCode(
            @Param("companyId") Long companyId,
            @Param("productCode") String productCode,
            @Param("excludeProductId") Long excludeProductId
    );

    int insert(Product product);

    int update(Product product);

    int updateUseYn(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("useYn") String useYn,
            @Param("updatedBy") Long updatedBy
    );
}