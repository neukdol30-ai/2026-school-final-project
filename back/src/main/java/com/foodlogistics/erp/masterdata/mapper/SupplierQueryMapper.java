package com.foodlogistics.erp.masterdata.mapper;


import com.foodlogistics.erp.masterdata.dto.SupplierOptionResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SupplierQueryMapper {

    @Select("""
            SELECT
                partner_id AS supplier_id,
                partner_code AS supplier_code,
                partner_name AS supplier_name
            FROM business_partner
            WHERE company_id = #{companyId}
              AND supplier_yn = 'Y'
              AND use_yn = 'Y'
              AND (
                    #{keyword, jdbcType=VARCHAR} IS NULL
                    OR LOWER(partner_name)
                    LIKE '%' || LOWER(
                        #{keyword, jdbcType=VARCHAR}
                    ) || '%'
                  )
            ORDER BY
                partner_name ASC,
                partner_id ASC
            """)
    @ConstructorArgs({
            @Arg(
                    column = "supplier_id",
                    javaType = Long.class,
                    id = true
            ),
            @Arg(
                    column = "supplier_code",
                    javaType = String.class
            ),
            @Arg(
                    column = "supplier_name",
                    javaType = String.class
            )
    })
    List<SupplierOptionResponse> findActiveSuppliers(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword
    );

}
