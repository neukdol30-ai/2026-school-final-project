package com.foodlogistics.erp.stocktake.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.stocktake.dto.*;
import com.foodlogistics.erp.stocktake.mapper.StocktakeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakeService {

    private final StocktakeMapper stocktakeMapper;
    private final StocktakeValidator stocktakeValidator;
    private final StocktakeCalculator stocktakeCalculator;
    private final StocktakeNumberGenerator stocktakeNumberGenerator;

    @Transactional
    public Long createStocktake(
            Long companyId,
            Long appUserId,
            StocktakeCreateRequestDto request
    ) {
        stocktakeValidator.validateUsableWarehouse(
                companyId,
                request.warehouseId()
        );

        stocktakeValidator.validateItems(
                companyId,
                request.items()
        );

        List<StocktakeItemSaveDto> itemsToSave =
                prepareItemsToSave(companyId, request);

        StocktakeSaveDto stocktakeToSave =
                new StocktakeSaveDto(
                        companyId,
                        stocktakeNumberGenerator.createStocktakeNo(),
                        request.warehouseId(),
                        request.memo(),
                        appUserId
                );

        // 실사 헤더를 먼저 저장해 stocktakeId를 생성
        stocktakeMapper.insertStocktake(stocktakeToSave);

        saveStocktakeItems(
                stocktakeToSave.getStocktakeId(),
                itemsToSave,
                appUserId
        );

        return stocktakeToSave.getStocktakeId();
    }

    @Transactional(readOnly = true)
    public List<StocktakeResponseDto> getStocktakeList(
            Long companyId
    ) {
        return stocktakeMapper.findAllByCompanyId(companyId);
    }

    // 재고실사 등록 화면의 창고 선택 목록
    @Transactional(readOnly = true)
    public List<StocktakeWarehouseOptionDto> getWarehouseOptions(
            Long companyId
    ) {
        return stocktakeMapper.findWarehouseOptions(companyId);
    }

    // 재고실사 등록 화면의 상품 선택 목록
    @Transactional(readOnly = true)
    public List<StocktakeProductOptionDto> getProductOptions(
            Long companyId
    ) {
        return stocktakeMapper.findProductOptions(companyId);
    }

    // 선택한 창고·상품에 실제로 연결된 LOT 재고 선택 목록
    @Transactional(readOnly = true)
    public List<StocktakeLotOptionDto> getLotOptions(
            Long companyId,
            Long warehouseId,
            Long productId
    ) {
        stocktakeValidator.validateUsableWarehouse(companyId, warehouseId);
        stocktakeValidator.validateUsableProduct(companyId, productId);

        return stocktakeMapper.findLotOptions(
                companyId,
                warehouseId,
                productId
        );
    }

    // 재고실사 헤더와 품목 목록을 합쳐 상세 정보를 반환한다.
    @Transactional(readOnly = true)
    public StocktakeDetailResponseDto getStocktakeDetail(
            Long companyId,
            Long stocktakeId
    ) {
        StocktakeDetailResponseDto detail =
                stocktakeMapper.findDetailById(companyId, stocktakeId);

        if (detail == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "재고실사를 찾을 수 없습니다."
            );
        }

        List<StocktakeItemResponseDto> items =
                stocktakeMapper.findItemsByStocktakeId(stocktakeId);

        return new StocktakeDetailResponseDto(
                detail.stocktakeId(),
                detail.stocktakeNo(),
                detail.warehouseId(),
                detail.warehouseName(),
                detail.stocktakeDate(),
                detail.status(),
                detail.memo(),
                items
        );
    }


    private List<StocktakeItemSaveDto> prepareItemsToSave(
            Long companyId,
            StocktakeCreateRequestDto request
    ) {
        List<StocktakeItemSaveDto> itemsToSave = new ArrayList<>();

        for (StocktakeItemCreateRequestDto item : request.items()) {
            BigDecimal systemQty = findSystemQty(
                    companyId,
                    request.warehouseId(),
                    item
            );

            itemsToSave.add(
                    stocktakeCalculator.calculateItem(item, systemQty)
            );
        }

        return itemsToSave;
    }

    private BigDecimal findSystemQty(
            Long companyId,
            Long warehouseId,
            StocktakeItemCreateRequestDto item
    ) {
        if (item.lotId() == null) {
            return stocktakeMapper.findStockQty(
                    companyId,
                    warehouseId,
                    item.productId()
            );
        }

        return stocktakeMapper.findLotStockQty(
                companyId,
                warehouseId,
                item.productId(),
                item.lotId()
        );
    }

    private void saveStocktakeItems(
            Long stocktakeId,
            List<StocktakeItemSaveDto> itemsToSave,
            Long appUserId
    ) {
        for (int index = 0; index < itemsToSave.size(); index++) {
            stocktakeMapper.insertStocktakeItem(
                    stocktakeId,
                    index + 1,
                    itemsToSave.get(index),
                    appUserId
            );
        }
    }
}
