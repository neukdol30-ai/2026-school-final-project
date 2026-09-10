package com.foodlogistics.erp.masterdata.mapper;

import com.foodlogistics.erp.masterdata.dto.WarehouseOptionResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WarehouseQueryMapper {

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
