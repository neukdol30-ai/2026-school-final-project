package com.foodlogistics.erp.stocktake.mapper;

import com.foodlogistics.erp.stocktake.dto.StocktakeItemSaveDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeResponseDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeSaveDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StocktakeMapper {

    // 이 회사에서 실제로 사용할 수 있는 창고인지 확인
    int countUsableWarehouse(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );

    // 이 회사에서 사용하는 상품인지 확인
    int countUsableProduct(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId
    );

    // 선택한 LOT가 해당 상품의 LOT인지 확인
    int countLotForProduct(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId
    );

    // LOT 비관리 상품의 현재 전산 재고 조회
    BigDecimal findStockQty(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    // LOT 관리 상품의 해당 LOT 전산 재고 조회
    BigDecimal findLotStockQty(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId
    );

    // STOCKTAKE 헤더 저장
    int insertStocktake(StocktakeSaveDto stocktake);

    // STOCKTAKE_ITEM 품목 한 줄 저장
    int insertStocktakeItem(
            @Param("stocktakeId") Long stocktakeId,
            @Param("lineNo") int lineNo,
            @Param("item") StocktakeItemSaveDto item,
            @Param("createdBy") Long createdBy
    );
    // 상품이 LOT 관리 대상인지 Y / N 조회
    String findLotManagedYn(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId
    );

    // 우리 회사 재고실사 목록 조회
    List<StocktakeResponseDto> findAllByCompanyId(
            @Param("companyId") Long companyId
    );
}