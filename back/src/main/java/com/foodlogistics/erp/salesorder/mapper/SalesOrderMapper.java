package com.foodlogistics.erp.salesorder.mapper;

import com.foodlogistics.erp.salesorder.dto.SalesOrderItemResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SalesOrderMapper {

    List<SalesOrderResponseDto> findAll();

    SalesOrderResponseDto findById(
            @Param("salesOrderId") Long salesOrderId
    );

    List<SalesOrderItemResponseDto> findItemsBySalesOrderId(
            @Param("salesOrderId") Long salesOrderId
    );
}