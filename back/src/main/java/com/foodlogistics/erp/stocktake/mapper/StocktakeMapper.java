package com.foodlogistics.erp.stocktake.mapper;

import com.foodlogistics.erp.stocktake.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StocktakeMapper {

    // 재고실사 등록 화면에서 선택할 사용 중인 창고 목록
    List<StocktakeWarehouseOptionDto> findWarehouseOptions(
            @Param("companyId") Long companyId
    );

    // 재고실사 등록 화면에서 선택할 사용 중인 상품 목록
    List<StocktakeProductOptionDto> findProductOptions(
            @Param("companyId") Long companyId
    );

    // 선택한 창고·상품의 LOT 재고 목록
    List<StocktakeLotOptionDto> findLotOptions(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

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

    // 재고실사 헤더 정보 한 건을 조회
    StocktakeDetailHeaderDto findDetailById(
            @Param("companyId") Long companyId,
            @Param("stocktakeId") Long stocktakeId
    );

    // 선택한 재고실사에 포함된 품목 목록을 조회
    List<StocktakeItemResponseDto> findItemsByStocktakeId(
            @Param("stocktakeId") Long stocktakeId
    );

    // 확정 처리에서 재고를 조정할 품목(차이 수량) 목록
    List<StocktakeConfirmItemDto> findConfirmItemsByStocktakeId(
            @Param("stocktakeId") Long stocktakeId
    );

    // DRAFT 상태인 실사 문서를 CONFIRMED로 전환한다.
    int confirmStocktake(
            @Param("companyId") Long companyId,
            @Param("stocktakeId") Long stocktakeId,
            @Param("confirmedBy") Long confirmedBy
    );

    // 수량이 음수가 되지 않는 경우에만 LOT 재고를 차이 수량만큼 조정한다.
    int adjustLotStockQuantity(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("differenceQty") BigDecimal differenceQty
    );

    // 수량이 음수가 되지 않는 경우에만 창고별 전체 재고를 차이 수량만큼 조정한다.
    int adjustStockQuantity(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("differenceQty") BigDecimal differenceQty
    );

    // 확정된 재고실사의 수량 차이를 재고 이력으로 남긴다.
    int insertStocktakeAdjustmentHistory(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("differenceQty") BigDecimal differenceQty,
            @Param("stocktakeId") Long stocktakeId,
            @Param("createdBy") Long createdBy
    );
}
