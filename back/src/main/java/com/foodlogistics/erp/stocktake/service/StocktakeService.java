package com.foodlogistics.erp.stocktake.service;

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

        // 실사 헤더를 먼저 저장해 stocktakeId를 생성한다.
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