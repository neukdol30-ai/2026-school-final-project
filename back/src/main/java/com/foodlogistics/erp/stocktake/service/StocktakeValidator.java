package com.foodlogistics.erp.stocktake.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.stocktake.dto.StocktakeItemCreateRequestDto;
import com.foodlogistics.erp.stocktake.mapper.StocktakeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class StocktakeValidator {

    private final StocktakeMapper stocktakeMapper;

    public void validateUsableWarehouse(
            Long companyId,
            Long warehouseId
    ) {
        if (stocktakeMapper.countUsableWarehouse(companyId, warehouseId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "사용 가능한 실사 창고가 아닙니다."
            );
        }
    }

    public void validateItems(
            Long companyId,
            List<StocktakeItemCreateRequestDto> items
    ) {
        Set<String> usedProductLots = new HashSet<>();

        for (StocktakeItemCreateRequestDto item : items) {
            validateUsableProduct(companyId, item.productId());

            String itemKey = item.productId() + ":" + item.lotId();

            // 같은 상품·LOT를 실사 문서에 두 번 넣는 것을 막는다.
            if (!usedProductLots.add(itemKey)) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "같은 상품과 LOT는 실사 품목에 한 번만 등록할 수 있습니다."
                );
            }

            validateLotInput(companyId, item);
        }
    }

    private void validateUsableProduct(
            Long companyId,
            Long productId
    ) {
        if (stocktakeMapper.countUsableProduct(companyId, productId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "사용 가능한 상품이 아닙니다."
            );
        }
    }

    private void validateLotInput(
            Long companyId,
            StocktakeItemCreateRequestDto item
    ) {
        String lotManagedYn = stocktakeMapper.findLotManagedYn(
                companyId,
                item.productId()
        );

        if ("Y".equals(lotManagedYn) && item.lotId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT 관리 상품은 LOT를 선택해야 합니다."
            );
        }

        if ("N".equals(lotManagedYn) && item.lotId() != null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT 비관리 상품에는 LOT를 선택할 수 없습니다."
            );
        }

        if (item.lotId() != null
                && stocktakeMapper.countLotForProduct(
                companyId,
                item.productId(),
                item.lotId()
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "선택한 상품에 속하지 않는 LOT입니다."
            );
        }
    }
}