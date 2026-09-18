package com.foodlogistics.erp.masterdata.mapper;

import com.foodlogistics.erp.masterdata.dto.AvailableProductUnitResponse;
import com.foodlogistics.erp.masterdata.dto.ProductOptionResponse;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductQueryMapper {

    @Select("""
            SELECT
                product_id,
                product_code,
                product_name,
                tax_type
            FROM product
            WHERE company_id = #{companyId}
              AND use_yn = 'Y'
              AND (
                    #{keyword, jdbcType=VARCHAR} IS NULL
                    OR LOWER(product_code)
                       LIKE '%' || LOWER(
                           #{keyword, jdbcType=VARCHAR}
                       ) || '%'
                    OR LOWER(product_name)
                       LIKE '%' || LOWER(
                           #{keyword, jdbcType=VARCHAR}
                       ) || '%'
                  )
            ORDER BY
                product_name ASC,
                product_id ASC
            """)
    @ConstructorArgs({
            @Arg(
                    column = "product_id",
                    javaType = Long.class,
                    id = true
            ),
            @Arg(
                    column = "product_code",
                    javaType = String.class
            ),
            @Arg(
                    column = "product_name",
                    javaType = String.class
            ),
            @Arg(
                    column = "tax_type",
                    javaType = String.class
            )
    })
    List<ProductOptionResponse> findActiveProducts(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword
    );


    @Select("""
            SELECT
                pu.product_unit_id,
                u.unit_code,
                u.unit_name,
                pu.conversion_qty,
                pu.is_base_yn
            FROM product p
            INNER JOIN product_unit pu
                ON pu.product_id = p.product_id
            INNER JOIN unit u
                ON u.unit_id = pu.unit_id
            WHERE p.product_id = #{productId}
              AND p.company_id = #{companyId}
              AND p.use_yn = 'Y'
              AND pu.use_yn = 'Y'
              AND u.use_yn = 'Y'
            ORDER BY
                CASE
                    WHEN pu.is_base_yn = 'Y' THEN 0
                    ELSE 1
                END,
                u.unit_name ASC,
                pu.product_unit_id ASC
            """)
    @ConstructorArgs({
            @Arg(
                    column = "product_unit_id",
                    javaType = Long.class,
                    id = true
            ),
            @Arg(
                    column = "unit_code",
                    javaType = String.class
            ),
            @Arg(
                    column = "unit_name",
                    javaType = String.class
            ),
            @Arg(
                    column = "conversion_qty",
                    javaType = BigDecimal.class
            ),
            @Arg(
                    column = "is_base_yn",
                    javaType = String.class
            )
    })
    List<AvailableProductUnitResponse>
    findAvailableProductUnits(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId
    );

}
