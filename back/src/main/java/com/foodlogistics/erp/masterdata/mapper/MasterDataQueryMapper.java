package com.foodlogistics.erp.masterdata.mapper;

import com.foodlogistics.erp.masterdata.dto.SupplierOptionResponse;
import com.foodlogistics.erp.masterdata.dto.WarehouseOptionResponse;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MasterDataQueryMapper {

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
                       LIKE '%' || LOWER(#{keyword}) || '%'
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

    @Select("""
            SELECT
                warehouse_id,
                warehouse_code,
                warehouse_name
            FROM warehouse
            WHERE company_id = #{companyId}
              AND use_yn = 'Y'
            ORDER BY
                warehouse_name ASC,
                warehouse_id ASC
            """)
    @ConstructorArgs({
            @Arg(
                    column = "warehouse_id",
                    javaType = Long.class,
                    id = true
            ),
            @Arg(
                    column = "warehouse_code",
                    javaType = String.class
            ),
            @Arg(
                    column = "warehouse_name",
                    javaType = String.class
            )
    })
    List<WarehouseOptionResponse> findActiveWarehouses(
            @Param("companyId") Long companyId
    );
}