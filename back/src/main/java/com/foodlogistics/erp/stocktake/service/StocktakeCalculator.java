package com.foodlogistics.erp.stocktake.service;


import com.foodlogistics.erp.stocktake.dto.StocktakeItemCreateRequestDto;
import com.foodlogistics.erp.stocktake.dto.StocktakeItemSaveDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StocktakeCalculator {

    public StocktakeItemSaveDto calculateItem(
            StocktakeItemCreateRequestDto request,
            BigDecimal systemQty
    ) {
        BigDecimal safeSystemQty =
                systemQty == null ? BigDecimal.ZERO : systemQty;

        BigDecimal differenceQty =
                request.actualQty().subtract(safeSystemQty);

        return new StocktakeItemSaveDto(
                request.productId(),
                request.lotId(),
                safeSystemQty,
                request.actualQty(),
                differenceQty,
                request.reason()
        );
    }
}
