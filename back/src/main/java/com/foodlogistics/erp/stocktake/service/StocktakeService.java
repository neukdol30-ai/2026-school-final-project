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

    // 확정 전 문서는 헤더와 품목을 새로 계산해 수정할 수 있다.
    @Transactional
    public Long updateStocktake(
            Long companyId,
            Long appUserId,
            Long stocktakeId,
            StocktakeCreateRequestDto request
    ) {
        StocktakeDetailHeaderDto existing = stocktakeMapper.findDetailById(
                companyId,
                stocktakeId
        );

        if (existing == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "재고실사를 찾을 수 없습니다."
            );
        }

        if (!"DRAFT".equals(existing.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 재고실사만 수정할 수 있습니다."
            );
        }

        stocktakeValidator.validateUsableWarehouse(companyId, request.warehouseId());
        stocktakeValidator.validateItems(companyId, request.items());

        List<StocktakeItemSaveDto> itemsToSave = prepareItemsToSave(companyId, request);

        if (stocktakeMapper.updateStocktakeHeader(
                companyId,
                stocktakeId,
                request.warehouseId(),
                request.memo(),
                appUserId
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 재고실사만 수정할 수 있습니다."
            );
        }

        stocktakeMapper.deleteStocktakeItems(companyId, stocktakeId);
        saveStocktakeItems(stocktakeId, itemsToSave, appUserId);

        return stocktakeId;
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

    // LOT 비관리 상품도 실사 전에 현재 전산 재고를 화면에서 확인할 수 있게 한다.
    @Transactional(readOnly = true)
    public BigDecimal getStockQuantity(
            Long companyId,
            Long warehouseId,
            Long productId
    ) {
        stocktakeValidator.validateUsableWarehouse(companyId, warehouseId);
        stocktakeValidator.validateUsableProduct(companyId, productId);

        BigDecimal quantity = stocktakeMapper.findStockQty(
                companyId,
                warehouseId,
                productId
        );

        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    // 재고실사 헤더와 품목 목록을 합쳐 상세 정보를 반환한다.
    @Transactional(readOnly = true)
    public StocktakeDetailResponseDto getStocktakeDetail(
            Long companyId,
            Long stocktakeId
    ) {
        StocktakeDetailHeaderDto detail =
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

    // 실사 등록 때 저장한 차이 수량만큼 재고를 조정하고, 문서를 확정한다.
    @Transactional
    public StocktakeDetailResponseDto confirmStocktake(
            Long companyId,
            Long appUserId,
            Long stocktakeId
    ) {
        StocktakeDetailHeaderDto stocktake =
                stocktakeMapper.findDetailById(companyId, stocktakeId);

        if (stocktake == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "재고실사를 찾을 수 없습니다."
            );
        }

        if (!"DRAFT".equals(stocktake.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 재고실사만 확정할 수 있습니다."
            );
        }

        List<StocktakeConfirmItemDto> items =
                stocktakeMapper.findConfirmItemsByStocktakeId(stocktakeId);

        if (items.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "실사 품목이 없는 재고실사는 확정할 수 없습니다."
            );
        }

        // 먼저 상태를 조건부로 바꿔 중복 확정을 막는다. 이후 실패하면 트랜잭션 전체가 롤백된다.
        if (stocktakeMapper.confirmStocktake(
                companyId,
                stocktakeId,
                appUserId
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 재고실사만 확정할 수 있습니다."
            );
        }

        for (StocktakeConfirmItemDto item : items) {
            applyStocktakeAdjustment(
                    companyId,
                    stocktake.warehouseId(),
                    stocktakeId,
                    appUserId,
                    item
            );
        }

        return getStocktakeDetail(companyId, stocktakeId);
    }

    @Transactional
    public void deleteStocktake(Long companyId, Long stocktakeId) {
        StocktakeDetailHeaderDto existing = stocktakeMapper.findDetailById(
                companyId,
                stocktakeId
        );

        if (existing == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "재고실사를 찾을 수 없습니다."
            );
        }

        if (!"DRAFT".equals(existing.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "확정된 재고실사는 삭제할 수 없습니다."
            );
        }

        stocktakeMapper.deleteStocktakeItems(companyId, stocktakeId);

        if (stocktakeMapper.deleteStocktake(companyId, stocktakeId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 재고실사만 삭제할 수 있습니다."
            );
        }
    }

    private void applyStocktakeAdjustment(
            Long companyId,
            Long warehouseId,
            Long stocktakeId,
            Long appUserId,
            StocktakeConfirmItemDto item
    ) {
        BigDecimal differenceQty = item.differenceQty();

        // 수량 차이가 없으면 재고 변경 및 조정 이력을 만들지 않는다.
        if (differenceQty.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        if (item.lotId() != null && stocktakeMapper.adjustLotStockQuantity(
                companyId,
                warehouseId,
                item.productId(),
                item.lotId(),
                differenceQty
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "LOT 재고가 없거나 조정 후 수량이 음수가 됩니다."
            );
        }

        if (stocktakeMapper.adjustStockQuantity(
                companyId,
                warehouseId,
                item.productId(),
                differenceQty
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "전체 재고가 없거나 조정 후 수량이 음수가 됩니다."
            );
        }

        stocktakeMapper.insertStocktakeAdjustmentHistory(
                companyId,
                warehouseId,
                item.productId(),
                item.lotId(),
                differenceQty,
                stocktakeId,
                appUserId
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
