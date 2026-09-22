package com.foodlogistics.erp.inventory.mapper;

import com.foodlogistics.erp.inventory.dto.InventoryStockResponse;
import com.foodlogistics.erp.inventory.dto.InventoryLotResponse;
import com.foodlogistics.erp.inventory.dto.InventoryHistoryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

// @Mapper는 이 인터페이스를 MyBatis 조회 객체로 등록한다.
// @Param의 이름은 InventoryMapper.xml의 바인딩 이름과 일치해야 한다.
@Mapper
public interface InventoryMapper {

    // STOCK: 창고·상품별 현재 총수량이며 LOT 수량과 더해서 계산하지 않는다.
    List<InventoryStockResponse> findStocks(
            @Param("companyId") Long companyId,
            @Param("warehouseKeyword") String warehouseKeyword,
            @Param("productKeyword") String productKeyword,
            @Param("includeZero") boolean includeZero
    );

    // LOT은 생산번호·날짜, LOT_STOCK은 그 LOT의 창고별 현재 수량을 가진다.
    List<InventoryLotResponse> findLots(
            @Param("companyId") Long companyId,
            @Param("warehouseKeyword") String warehouseKeyword,
            @Param("productKeyword") String productKeyword,
            @Param("lotNo") String lotNo,
            @Param("includeZero") boolean includeZero
    );

    // STOCK_HISTORY: 현재고를 계산하는 입력이 아니라 증감 원인을 추적하는 기존 이력이다.
    List<InventoryHistoryResponse> findHistory(
            @Param("companyId") Long companyId,
            @Param("warehouseKeyword") String warehouseKeyword,
            @Param("productKeyword") String productKeyword,
            @Param("lotNo") String lotNo,
            @Param("movementType") String movementType,
            @Param("startAt") LocalDateTime startAt,
            @Param("endBefore") LocalDateTime endBefore
    );
}
