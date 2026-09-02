package com.foodlogistics.erp.salesorder.mapper;

import com.foodlogistics.erp.salesorder.dto.*;
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

    SalesOrderItemOrderInfoDto findOrderItemInfoByProductUnitId(
            @Param("productUnitId") Long productUnitId
    );

    int countUsableCustomer(
            @Param("companyId") Long companyId,
            @Param("customerId") Long customerId
    );

    int countUsableWarehouse(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );

    int insertSalesOrder(SalesOrderSaveDto salesOrder);

    int insertSalesOrderItem(
            @Param("salesOrderId") Long salesOrderId,
            @Param("lineNo") int lineNo,
            @Param("item")SalesOrderItemSaveDto item,
            @Param("createdBy") Long createdBy
            );

    int confirmSalesOrder(
            @Param("salesOrderId") Long salesOrderId,
            @Param("companyId") Long companyId,
            @Param("confirmedBy") Long confirmedBy
    );
}